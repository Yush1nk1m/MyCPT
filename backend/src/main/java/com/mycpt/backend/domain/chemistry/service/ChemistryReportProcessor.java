package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.notification.service.NotificationService;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import com.mycpt.backend.domain.statistics.repository.StatisticsRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private final ChemistryReportRepository chemistryReportRepository;
    private final StatisticsRepository statisticsRepository;
    private final ChemistryCacheService chemistryCacheService;
    private final NotificationService notificationService;

    @Async
    @Retryable(
            retryFor = Exception.class,
            noRetryFor = IllegalStateException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Transactional
    public void process(Long reportId) {
        ChemistryReport report = chemistryReportRepository.findByIdWithUsers(reportId)
                .orElseThrow(() -> new IllegalStateException("ChemistryReport 없음: " + reportId));

        User requester = report.getRequester();
        User partner = report.getPartner();

        List<LatestBuckets> requesterBuckets = statisticsRepository.findLatestBuckets(
                requester.getId(), RaterType.SELF, PageRequest.of(0, 1));
        List<LatestBuckets> partnerBuckets = statisticsRepository.findLatestBuckets(
                partner.getId(), RaterType.SELF, PageRequest.of(0, 1));

        if (requesterBuckets.isEmpty() || partnerBuckets.isEmpty()) {
            throw new IllegalStateException("검사 결과 없음. reportId=" + reportId);
        }

        LatestBuckets rb = requesterBuckets.get(0);
        LatestBuckets pb = partnerBuckets.get(0);

        ChemistryCacheId cacheId = new ChemistryCacheId(
                rb.dBucket(), rb.iBucket(), rb.sBucket(), rb.cBucket(),
                pb.dBucket(), pb.iBucket(), pb.sBucket(), pb.cBucket()
        );

        // 발행자/구독자 분기 - 보고서 텍스트 반환 (캐시 히트 or LLM 생성 or 구독 대기)
        chemistryCacheService.getOrGenerate(cacheId, requester.getId(), reportId, rb, pb);

        // chemistry_reports READY 업데이트
        completeReport(reportId, cacheId);

        // 상대방에게 인앱 알림 전송
        // SSE push는 ChemistryEventSubscriber가 Pub/Sub 수신 후 처리
        notificationService.sendChemistryNotification(partner, report, requester);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeReport(Long reportId, ChemistryCacheId cacheId) {
        chemistryReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("ChemistryReport 없음: " + reportId))
                .complete(cacheId);
    }

    @Recover
    @Transactional
    public void recover(Exception e, Long reportId) {
        log.error("케미 보고서 생성 최종 실패. reportId={}", reportId, e);
        chemistryReportRepository.findById(reportId)
                .ifPresent(r -> {
                    r.fail();
                    // TODO: 코인 환불 로직 구현
                    log.warn("케미 보고서 실패 처리 완료. reportId={}, requesterId={}",
                            r.getId(), r.getRequester().getId());
                });
    }
}
