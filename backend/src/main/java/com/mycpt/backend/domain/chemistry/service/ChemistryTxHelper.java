package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import com.mycpt.backend.domain.chemistry.repository.ChemistryCacheRepository;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * chemistry_cache 락 트랜잭션 전담 컴포넌트.
 *
 * ChemistryCacheService의 자가 호출 문제를 해결하기 위해 분리.
 * 이 클래스의 모든 메서드는 REQUIRES_NEW — 독립 트랜잭션으로 실행되어
 * 커밋 즉시 SELECT FOR UPDATE 락이 해제됨.
 */
@Component
@RequiredArgsConstructor
public class ChemistryTxHelper {

    private final ChemistryReportRepository chemistryReportRepository;
    private final ChemistryCacheRepository chemistryCacheRepository;

    /**
     * SELECT FOR UPDATE -> 역할 결정 -> 발행자면 GENERATING 업데이트 -> 커밋(락 해제)
     *
     * @return 진입 시점의 status (역할 결정 기준)
     *         만료 캐시는 refresh() 후 NULL 반환 -> 발행자로 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChemistryCacheStatus acquireLockAndDecideRole(
            ChemistryCacheId cacheId,
            long ttlDays,
            Runnable onSubscriber   // 구독자 결정 시 대기자 맵 등록 콜백
    ) {
        ChemistryCache cache = chemistryCacheRepository.findByIdWithLock(cacheId)
                .orElseThrow(() -> new IllegalStateException("chemistry_cache 행 없음: " + cacheId));

        ChemistryCacheStatus currentStatus = cache.getStatus();

        switch (currentStatus) {
            case NULL -> cache.startGenerating();
            case GENERATING -> onSubscriber.run();  // 대기자 맵 등록
            case READY -> {
                LocalDateTime expireLine = LocalDateTime.now().minusDays(ttlDays);
                if (cache.getCreatedAt() != null && cache.getCreatedAt().isBefore(expireLine)) {
                    cache.refresh();
                    return ChemistryCacheStatus.NULL;   // 발행자로 재진입
                }
            }
        }

        return currentStatus;
    }

    /**
     * LLM 완료 후 READY 업데이트 -> 커밋(락 해제) -> 이후 Pub/Sub 발행
     * SELECT FOR UPDATE로 원자성 보장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCompletedCache(ChemistryCacheId cacheId, String report) {
        ChemistryCache cache = chemistryCacheRepository.findByIdWithLock(cacheId)
                .orElseThrow(() -> new IllegalStateException("chemistry_cache 행 없음: " + cacheId));
        cache.complete(report, LocalDateTime.now());
    }

    /**
     * chemistry_reports READY 업데이트 — REQUIRES_NEW 독립 트랜잭션.
     * self-invocation 방지를 위해 ChemistryCacheLockTx에 위치.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeReport(Long reportId, ChemistryCacheId cacheId) {
        chemistryReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("ChemistryReport 없음: " + reportId))
                .complete(cacheId);
    }
}
