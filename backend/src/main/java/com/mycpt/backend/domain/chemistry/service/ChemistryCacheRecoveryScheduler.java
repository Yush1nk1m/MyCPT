package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.llm.AnthropicLlmClient;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import com.mycpt.backend.domain.chemistry.event.ChemistryEventPublisher;
import com.mycpt.backend.domain.chemistry.repository.ChemistryCacheRepository;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * chemistry_cache에서 GENERATING인 채로 오래 멈춰있는 행을 찾아 재생성을 재시도하는 배치.
 *
 * "오래됨"의 기준은 updated_at.
 * 발행자가 락을 잡는 순간(startGenerating/refresh) 또는 이 배치가 재시도를 시작하는 순간(markRetryStarted)
 * 갱신되므로, 한 번 집어든 행은 최소 STALE_THRESHOLD_MINUTES 동안 재선택되지 않음
 * (배치 중복 실행 방지 겸용, cacheId를 NULL로 되돌리지 않아 진행 중인 실유저 요청과의 경합도 없음).
 *
 * 재생성 성공 시 같은 버킷 조합으로 ERROR 처리됐던 chemistry_reports도 조용히 READY로 교정.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChemistryCacheRecoveryScheduler {

    private static final long STALE_THRESHOLD_MINUTES = 10;

    private final ChemistryCacheRepository chemistryCacheRepository;
    private final ChemistryTxHelper txHelper;
    private final AnthropicLlmClient llmClient;
    private final ChemistryEventPublisher eventPublisher;
    private final ChemistryCacheService chemistryCacheService;

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void recoverStaleGeneratingCaches() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(STALE_THRESHOLD_MINUTES);
        List<ChemistryCache> staleCaches = chemistryCacheRepository
                .findByStatusAndUpdatedAtBefore(ChemistryCacheStatus.GENERATING, cutoff);

        if (staleCaches.isEmpty()) {
            return;
        }

        log.info("스테일 GENERATING 캐시 {}건 발견. 재시도 시작.", staleCaches.size());
        for (ChemistryCache stale : staleCaches) {
            recoverOne(stale.getId());
        }
    }

    private void recoverOne(ChemistryCacheId cacheId) {
        boolean claimed = txHelper.claimForRetry(cacheId, STALE_THRESHOLD_MINUTES);
        if (!claimed) {
            return;
        }

        try {
            LatestBuckets requesterBuckets = new LatestBuckets(
                    cacheId.getRequesterD(), cacheId.getRequesterI(), cacheId.getRequesterS(), cacheId.getRequesterC()
            );
            LatestBuckets partnerBuckets = new LatestBuckets(
                    cacheId.getPartnerD(), cacheId.getPartnerI(), cacheId.getPartnerS(), cacheId.getPartnerC()
            );

            String prompt = chemistryCacheService.buildPrompt(requesterBuckets, partnerBuckets);
            String report = llmClient.complete(prompt);

            txHelper.saveCompletedCache(cacheId, report);
            eventPublisher.publishReady(cacheId);
            txHelper.reconcileErrorReports(cacheId);

            log.info("스테일 캐시 복구 완료. cacheId={}", cacheId);
        } catch (Exception e) {
            log.warn("스테일 캐시 복구 실패. 다음 주기 재시도. cacheID={}", cacheId, e);
        }
    }
}
