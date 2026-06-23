package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.llm.AnthropicLlmClient;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.repository.ChemistryCacheRepository;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * chemistry_cache Lazy Caching 서비스.
 *
 * disc_cache(CacheService)와 동일한 히트/미스/만료 분기.
 * 차이점:
 *   - 사전 삽입 없음 → 미스 시 INSERT (disc_cache는 UPDATE만)
 *   - 복합 PK 8축 (requester 4 + partner 4)
 *
 * 분기:
 *   [미스] findById() empty → LLM 호출 → INSERT → 반환
 *   [히트 + 유효] created_at >= expireLine → 즉시 반환
 *   [히트 + 만료] created_at < expireLine → LLM 호출 → UPDATE → 반환
 */
@Service
public class ChemistryCacheService {

    private final ChemistryCacheRepository chemistryCacheRepository;
    private final AnthropicLlmClient llmClient;
    private final long ttlDays;

    public ChemistryCacheService(
            ChemistryCacheRepository chemistryCacheRepository,
            AnthropicLlmClient llmClient,
            @Value("${cache.chemistry.ttl-days:365}") long ttlDays
    ) {
        this.chemistryCacheRepository = chemistryCacheRepository;
        this.llmClient = llmClient;
        this.ttlDays = ttlDays;
    }

    /**
     * 두 사람의 버킷값 조합에 해당하는 케미 보고서를 반환.
     * 캐시 미스/만료 시 LLM 호출 후 저장.
     *
     * @param requester requester의 버킷값
     * @param partner   partner의 버킷값
     * @return Markdown 형식 케미 보고서
     */
    @Transactional
    public String getReport(LatestBuckets requester, LatestBuckets partner) {
        ChemistryCacheId id = new ChemistryCacheId(
                requester.dBucket(), requester.iBucket(),
                requester.sBucket(), requester.cBucket(),
                partner.dBucket(), partner.iBucket(),
                partner.sBucket(), partner.cBucket()
        );
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireLine = now.minusDays(ttlDays);

        Optional<ChemistryCache> cached = chemistryCacheRepository.findById(id);

        // 미스: INSERT
        if (cached.isEmpty()) {
            String report = llmClient.complete(buildPrompt(requester, partner));
            chemistryCacheRepository.save(ChemistryCache.create(id, report, now));
            return report;
        }

        ChemistryCache cache = cached.get();

        // 히트 + 유효
        if (!cache.getCreatedAt().isBefore(expireLine)) {
            return cache.getReport();
        }

        // 히트 + 만료: UPDATE
        String report = llmClient.complete(buildPrompt(requester, partner));
        cache.refresh(report, now);
        chemistryCacheRepository.save(cache);
        return report;
    }

    private String buildPrompt(LatestBuckets a, LatestBuckets b) {
        return """
                당신은 DISC 성격 유형 전문 분석가입니다.
                두 사람의 DISC 버킷값을 기반으로 한국어 케미 보고서를 작성하세요.

                DISC 버킷 척도:
                - 1 (Low)  : 해당 성향을 의도적으로 기피하거나 거부함
                - 2 (Mid)  : 상황에 따라 유연하게 발현되는 중간 성향
                - 3 (High) : 가장 확실하게 드러나는 주성향

                [A(발행자)의 DISC 버킷값]
                D(주도형): %d, I(사교형): %d, S(안정형): %d, C(신중형): %d

                [B(상대방)의 DISC 버킷값]
                D(주도형): %d, I(사교형): %d, S(안정형): %d, C(신중형): %d

                아래 6개 섹션을 Markdown 형식으로 작성하세요.
                각 섹션은 3~5문장으로 구성하고, 이름 없이 "A유형의 사람"/"B유형의 사람"으로 서술하세요.

                [제약 사항]
                - 보고서 제목, 버킷값 요약 등 어떠한 머리말도 작성하지 마세요.
                - 반드시 ## 두 사람의 성향 요약 섹션으로 시작하세요.
                - 6개 섹션 외의 내용은 절대 추가하지 마세요.

                ## 두 사람의 성향 요약
                ## 협업 시너지
                ## 갈등 포인트
                ## 소통 방식 차이
                ## 서로에게 맞는 역할
                ## 관계 발전을 위한 제안
            """.formatted(
                    a.dBucket(), a.iBucket(), a.sBucket(), a.cBucket(),
                b.dBucket(), b.iBucket(), b.sBucket(), b.cBucket()
        );
    }
}
