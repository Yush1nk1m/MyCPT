package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.notification.service.NotificationService;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import com.mycpt.backend.domain.statistics.repository.StatisticsRepository;
import com.mycpt.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 케미 보고서 비동기 생성 프로세서.
 *
 * ChemistryService가 202 반환 직후 트리거.
 * @Async로 별도 스레드에서 실행 — 호출자 트랜잭션과 분리됨.
 *
 * 처리 흐름:
 *   1. reportId로 ChemistryReport 조회 (requester, partner JOIN FETCH)
 *   2. 두 사람 최신 자기 평정 버킷 조회
 *   3. ChemistryCacheService로 보고서 조회 (히트/미스/만료 분기)
 *   4. report.complete() → 알림 전송
 *
 * @Retryable: 최대 3회, 2초→4초→8초 지수 백오프
 * 소진 시 @Recover → report.fail() 처리
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
                .orElseThrow(() -> new IllegalStateException("ChemistryReport Not Found: " + reportId));

        User requester = report.getRequester();
        User partner = report.getPartner();

        List<LatestBuckets> requesterBuckets = statisticsRepository.findLatestBuckets(
                requester.getId(), RaterType.SELF, PageRequest.of(0, 1));
        List<LatestBuckets> partnerBuckets = statisticsRepository.findLatestBuckets(
                partner.getId(), RaterType.SELF, PageRequest.of(0, 1));

        if (requesterBuckets.isEmpty() || partnerBuckets.isEmpty()) {
            // 검사 결과 없음 -> 실패 처리
            // TODO: 코인 환불 로직 구
            handleFailure(report, new IllegalStateException("검사 결과 없음"));
            return;
        }

        // 캐시 조회 - 히트 시 즉시 반환, 미스/만료 시 LLM 호출
        String generatedReport = chemistryCacheService.getReport(
                requesterBuckets.get(0),
                partnerBuckets.get(0)
        );

        report.complete(generatedReport);

        // 상대방에게 알림 전송
        // TODO: SSE 푸시 구현
        notificationService.sendChemistryNotification(partner, report, requester);
    }

    @Recover
    @Transactional
    public void recover(Exception e, Long reportId) {
        log.error("케미 보고서 생성 최종 실패. reportId={}", reportId, e);
        chemistryReportRepository.findById(reportId)
                .ifPresent(r -> handleFailure(r, e));
    }

    private void handleFailure(ChemistryReport report, Exception e) {
        report.fail();
        // TODO: 코인 환불 로직 구현
        log.warn("케미 보고서 실패 처리. reportId={}, requesterId={}",
                report.getId(), report.getRequester().getId());
    }
}
