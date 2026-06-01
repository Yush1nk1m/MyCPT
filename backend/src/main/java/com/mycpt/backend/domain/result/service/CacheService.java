package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.domain.result.entity.DiscCache;
import com.mycpt.backend.domain.result.entity.DiscCacheId;
import com.mycpt.backend.domain.result.repository.DiscCacheRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * disc_cache Lazy Caching 서비스
 *
 * 흐름 (sequence-scoring.puml 기준):
 *
 *  [MISS]
 *      DB에 행 없음 -> LLM 호출 -> INSERT -> 반환
 *
 *  [HIT + 유효]
 *      created_at >= (now - ttlDays) -> 즉시 반환
 *
 *  [HIT + 만료]
 *      created_at < (now - ttlDays) -> LLM 호출 -> refresh() -> UPDATE -> 반환
 *      * 행 DELETE 없이 UPDATE만 사용 - disc_results -> disc_cache FK 무결성 유지
 *
 * 설정 값 (application.properties):
 *  cache.disc.ttl-days=365  (기본 값 365일)
 */
@Service
public class CacheService {

    private final DiscCacheRepository discCacheRepository;
    private final LlmService llmService;
    private final long ttlDays;

    public CacheService(
            DiscCacheRepository discCacheRepository,
            LlmService llmService,
            @Value("${cache.disc.ttl-days:365}") long ttlDays
    ) {
        this.discCacheRepository = discCacheRepository;
        this.llmService = llmService;
        this.ttlDays = ttlDays;
    }

    /**
     * 버킷 값 조합에 해당하는 보고서를 반환
     * HIT/MISS 분기 및 만료 처리를 담당하는 핵심 메서드
     *
     * @param buckets ScoringService가 반환한 버킷 값 (d, i, s, c 각 1~3)
     * @return Markdown 형식 분석 보고서
     */
    @Transactional
    public String getReport(ScoringService.Buckets buckets) {
        DiscCacheId id = new DiscCacheId(buckets.d(), buckets.i(), buckets.s(), buckets.c());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireLine = now.minusDays(ttlDays);  // 만료 기준선

        Optional<DiscCache> cached = discCacheRepository.findById(id);

        if (cached.isEmpty()) {
            // ── MISS: 이 버킷 조합이 DB에 없음 ──────────────────────────────
            return insertNewCache(id, now);
        }

        DiscCache hit = cached.get();

        if (hit.getCreatedAt().isBefore(expireLine)) {
            // ── HIT + 만료: created_at이 기준선보다 오래됨 ───────────────────
            return refreshExpiredCache(hit, now);
        }

        // ── HIT + 유효: 즉시 반환 ────────────────────────────────────────────
        return hit.getReport();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * [MISS] LLM 호출 -> 새 행 INSERT -> 보고서 반환
     */
    private String insertNewCache(DiscCacheId id, LocalDateTime now) {
        String report = llmService.generateReport(id);
        discCacheRepository.save(new DiscCache(id, report, now));
        return report;
    }

    /**
     * [HIT + 만료] LLM 호출 -> 기존 행 refresh() -> UPDATE -> 보고서 반환
     * save()를 명시적으로 호출해 의도를 드러냄
     * (@Transactional dirty checking으로도 UPDATE되지만 명시가 더 안전한 방식)
     */
    private String refreshExpiredCache(DiscCache hit, LocalDateTime now) {
        String newReport = llmService.generateReport(hit.getId());
        hit.refresh(newReport, now);
        discCacheRepository.save(hit);
        return newReport;
    }
}
