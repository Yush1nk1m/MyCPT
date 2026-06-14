package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.domain.result.entity.DiscCache;
import com.mycpt.backend.domain.result.entity.DiscCacheId;
import com.mycpt.backend.domain.result.repository.DiscCacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * disc_cache Lazy Caching 서비스
 *
 * 사전 조건: 초기화 스크립트(schema.sql)가 81개 행을 report=NULL로 삽입해둠.
 * → findById()는 항상 행을 반환함. empty는 데이터 정합성 오류.
 *
 * 분기 (sequence-scoring.puml 기준):
 *
 *  [미생성] report == NULL
 *      LLM 호출 → UPDATE → 반환
 *
 *  [HIT + 유효]
 *      created_at >= (now - ttlDays) → 즉시 반환
 *
 *  [HIT + 만료]
 *      created_at < (now - ttlDays) → LLM 호출 → UPDATE → 반환
 *
 * 설정 값 (application.properties):
 *  cache.disc.ttl-days=365  (기본값 365일)
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
     * @throws IllegalStateException 초기화 스크립트 미실행 등으로 행이 없는 경우
     */
    @Transactional
    public String getReport(ScoringService.Buckets buckets) {
        DiscCacheId id = new DiscCacheId(buckets.d(), buckets.i(), buckets.s(), buckets.c());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireLine = now.minusDays(ttlDays);  // 만료 기준선

        // 사전 삽입으로 항상 행이 존재함. empty는 데이터 정합성 오류 -> 예외 발생
        DiscCache cache = discCacheRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "disc_cache 행 누락: d=%d i=%d s=%d c=%d. 초기화 스크립트를 확인하세요."
                                .formatted(buckets.d(), buckets.i(), buckets.s(), buckets.c())));

        // 미생성(NULL) 또는 만료 -> LLM 호출 후 UPDATE
        if (cache.getReport() == null || cache.getCreatedAt().isBefore(expireLine)) {
            return refreshCache(cache, now);
        }

        // 유효 -> 즉시 반환
        return cache.getReport();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * LLM 호출 → report/created_at UPDATE → 보고서 반환
     * 미생성(NULL)과 만료 양쪽에서 공통으로 사용
     */
    private String refreshCache(DiscCache cache, LocalDateTime now) {
        String newReport = llmService.generateReport(cache.getId());
        cache.refresh(newReport, now);
        discCacheRepository.save(cache);
        return newReport;
    }
}
