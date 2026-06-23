package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.common.llm.AnthropicLlmClient;
import com.mycpt.backend.domain.result.entity.DiscCache;
import com.mycpt.backend.domain.result.entity.DiscCacheId;
import com.mycpt.backend.domain.result.repository.DiscCacheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
public class DiscCacheService {

    private final DiscCacheRepository discCacheRepository;
    private final AnthropicLlmClient llmClient;
    private final long ttlDays;

    public DiscCacheService(
            DiscCacheRepository discCacheRepository,
            AnthropicLlmClient llmClient,
            @Value("${cache.disc.ttl-days:365}") long ttlDays
    ) {
        this.discCacheRepository = discCacheRepository;
        this.llmClient = llmClient;
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
            String report = llmClient.complete(buildPrompt(id));
            cache.refresh(report, now);
            discCacheRepository.save(cache);
            return report;
        }

        // 유효 -> 즉시 반환
        return cache.getReport();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private String buildPrompt(DiscCacheId id) {
        return """
            당신은 DISC 성격 유형 전문 분석가입니다.
            다음 DISC 버킷값을 기반으로 한국어 분석 보고서를 작성하세요.
    
            DISC 버킷값 (3구간 척도):
            - 1 (Low)  : 해당 성향을 의도적으로 기피하거나 거부함
            - 2 (Mid)  : 상황에 따라 유연하게 발현되는 중간 성향
            - 3 (High) : 가장 확실하게 드러나는 주성향
    
            측정값:
            - D (주도형 / Dominance):         %d
            - I (사교형 / Influence):         %d
            - S (안정형 / Steadiness):        %d
            - C (신중형 / Conscientiousness): %d
    
            아래 6개 섹션을 Markdown 형식으로 작성하세요.
            각 섹션은 3~5문장으로 구성하고, 특정 이름 없이 "이 유형의 사람은"으로 서술하세요.
    
            [제약 사항]
            - 보고서 제목, 유형 분류, 측정값 요약 등 어떠한 머리말도 작성하지 마세요.
            - 반드시 ## 결과 개요 섹션으로 시작하세요.
            - 6개 섹션 외의 내용은 절대 추가하지 마세요.
    
            ## 결과 개요
            ## 강점
            ## 약점 및 주의할 점
            ## 동료와의 협업 스타일
            ## 스트레스 상황에서의 반응
            ## 성장을 위한 제안
        """.formatted(id.getD(), id.getI(), id.getS(), id.getC());
    }
}
