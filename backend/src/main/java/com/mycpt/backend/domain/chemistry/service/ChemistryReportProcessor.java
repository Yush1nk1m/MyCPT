package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.event.ChemistryReportIssuedEvent;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.notification.service.NotificationService;
import com.mycpt.backend.domain.notification.service.SseService;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import com.mycpt.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 케미 보고서 비동기 생성 프로세서.
 *
 * ChemistryService.issue() 트랜잭션 커밋 후 @Async 별도 스레드에서 실행.
 *
 * 처리 흐름:
 *   1. reportId로 ChemistryReport 조회 (requester, partner JOIN FETCH)
 *   2. 두 사람 최신 자기 평정 버킷 조회
 *   3. ChemistryCacheService.getOrGenerate() — 발행자/구독자 분기, 보고서 반환
 *   4. report.complete(cacheId) → chemistry_reports READY 업데이트
 *   5. 알림 전송
 *
 * @Retryable 범위:
 *   - IllegalStateException(검사 결과 없음, 행 없음 등 재시도 불가 케이스) 제외
 *   - LLM 호출 실패 등 일시적 오류만 재시도
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChemistryReportProcessor {

    private final ChemistryTxHelper txHelper;
    private final ChemistryReportRepository chemistryReportRepository;
    private final ChemistryCacheService chemistryCacheService;
    private final NotificationService notificationService;
    private final SseService sseService;

    public void process(Long reportId, LatestBuckets requesterBuckets, LatestBuckets partnerBuckets) {
        ChemistryReport report = chemistryReportRepository.findByIdWithUsers(reportId)
                .orElseThrow(() -> new IllegalStateException("ChemistryReport 없음: " + reportId));

        ChemistryCacheId cacheId = report.getCacheId();
        User requester = report.getRequester();
        User partner = report.getPartner();

        // 버킷은 호출자에서 이미 조회됨 - 재조회 없음
        chemistryCacheService.getOrGenerate(cacheId, requester.getId(), reportId, requesterBuckets, partnerBuckets);

        // chemistry_reports READY 업데이트
        txHelper.completeReport(reportId, cacheId);

        // 발행자 본인에게 SSE push
        // - 구독자(waitingMap 등록된 동일 버킷 요청자)는 ChemistryEventSubscriber가 처리
        // - 발행자와 캐시 READY 즉시 반환 케이스는 여기서 직접 push
        sseService.pushChemistryReady(requester.getId(), reportId);

        // 상대방에게 인앱 알림 전송
        // SSE push는 ChemistryEventSubscriber가 Pub/Sub 수신 후 처리
        notificationService.sendChemistryNotification(partner, report, requester);
    }


    @Recover
    @Transactional
    public void recover(Exception e, ChemistryReportIssuedEvent event) {
        Long reportId = event.reportId();
        log.error("케미 보고서 생성 최종 실패. reportId={}", reportId, e);
        chemistryReportRepository.findById(reportId)
                .ifPresent(r -> {
                    r.fail();
                    // TODO: 코인 환불 로직 구현
                    log.warn("케미 보고서 실패 처리 완료. reportId={}, requesterId={}",
                            r.getId(), r.getRequester().getId());
                });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Retryable(
            retryFor = Exception.class,
            noRetryFor = IllegalStateException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void handle(ChemistryReportIssuedEvent event) {
        process(event.reportId(), event.requesterBuckets(), event.partnerBuckets());
    }
}
