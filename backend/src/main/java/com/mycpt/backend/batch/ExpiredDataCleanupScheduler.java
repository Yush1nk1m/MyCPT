package com.mycpt.backend.batch;

import com.mycpt.backend.domain.assessment.repository.AssessmentTokenRepository;
import com.mycpt.backend.domain.colleague.repository.PeerCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 만료 동료 코드 + 만료 평정 토큰 통합 삭제.
 * database-design.md §5 문서화된 배치 — ChemistryCacheRecoveryScheduler와 동일하게
 * 순수 @Scheduled 방식(Spring Batch 미사용, 07.03 결정 — 규모 대비 오버엔지니어링).
 * 두 삭제를 하나의 트랜잭션으로 묶어 원자적으로 처리.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredDataCleanupScheduler {

    private final PeerCodeRepository peerCodeRepository;
    private final AssessmentTokenRepository assessmentTokenRepository;

    @Scheduled(cron = "${expired-data-cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        long deletedPeerCodes = peerCodeRepository.deleteByExpiresAtBefore(now);
        long deletedTokens = assessmentTokenRepository.deleteByExpiresAtBefore(now);
        log.info("만료 데이터 삭제 완료. peerCodes={}건, assessmentTokens={}건", deletedPeerCodes, deletedTokens);
    }
}
