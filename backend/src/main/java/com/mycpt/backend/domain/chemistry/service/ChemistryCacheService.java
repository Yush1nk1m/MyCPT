package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.llm.AnthropicLlmClient;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import com.mycpt.backend.domain.chemistry.event.ChemistryEventPublisher;
import com.mycpt.backend.domain.chemistry.repository.ChemistryCacheRepository;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * chemistry_cache 락 기반 중복 LLM 호출 방지 서비스.
 *
 * 발행자/구독자 분기:
 *   [NULL]       → 발행자: GENERATING으로 변경 후 즉시 커밋(락 해제) → LLM 호출 → 완료 커밋 → Pub/Sub
 *   [GENERATING] → 구독자: 대기자 맵 등록 후 CountDownLatch로 완료 대기
 *   [READY+유효] → 즉시 캐시 반환
 *   [READY+만료] → 발행자 경로 재진입 (refresh() 후 재생성)
 *
 * 인메모리 맵:
 *   waitingMap: Map<ChemistryCacheId, List<WaitingEntry>> — 버킷 조합별 대기자 목록
 *   SseEmitter 맵은 SseService가 별도 소유 (관심사 분리)
 */
@Slf4j
@Service
public class ChemistryCacheService {

    private final ChemistryCacheRepository chemistryCacheRepository;
    private final AnthropicLlmClient llmClient;
    private final ChemistryEventPublisher eventPublisher;
    private final ChemistryTxHelper txHelper;
    private final long ttlDays;

    // 버킷 조합 -> 대기 중인 (userId, reportId, latch) 목록
    private final Map<ChemistryCacheId, List<WaitingEntry>> waitingMap = new ConcurrentHashMap<>();

    public ChemistryCacheService(
            ChemistryCacheRepository chemistryCacheRepository,
            AnthropicLlmClient llmClient,
            ChemistryEventPublisher eventPublisher,
            ChemistryTxHelper txHelper,
            @Value("${cache.chemistry.ttl-days:365}") long ttlDays
    ) {
        this.chemistryCacheRepository = chemistryCacheRepository;
        this.llmClient = llmClient;
        this.eventPublisher = eventPublisher;
        this.txHelper = txHelper;
        this.ttlDays = ttlDays;
    }

    /**
     * 케미 보고서 요청 진입점
     *
     * @return 완성된 보고서 텍스트
     */
    public String getOrGenerate(
            ChemistryCacheId cacheId,
            Long userId,
            Long reportId,
            LatestBuckets requesterBuckets,
            LatestBuckets partnerBuckets
    ) {
        // 콜백으로 구독자 등록 - 락 트랜잭션 내부에서 실행
        ChemistryCacheStatus status = txHelper.acquireLockAndDecideRole(
                cacheId,
                ttlDays,
                () -> registerWaiter(cacheId, userId, reportId)
        );

        return switch (status) {
            case NULL -> generateAsPublisher(cacheId, requesterBuckets, partnerBuckets);
            case GENERATING -> waitAsSubscriber(cacheId, userId, reportId);
            case READY -> chemistryCacheRepository.findById(cacheId)
                    .map(ChemistryCache::getReport)
                    .orElseThrow(() -> new IllegalStateException("READY 상태이지만 캐시 행 없음: " + cacheId));
        };
    }

    /**
     * 발행자 경로 - LLM 호출 -> 완료 커밋 -> Pub/Sub 발행
     */
    private String generateAsPublisher(
            ChemistryCacheId cacheId,
            LatestBuckets requesterBuckets,
            LatestBuckets partnerBuckets
    ) {
        String prompt = buildPrompt(requesterBuckets, partnerBuckets);
        String report = llmClient.complete(prompt);
        txHelper.saveCompletedCache(cacheId, report);
        eventPublisher.publishReady(cacheId);
        return report;
    }

    /**
     * 구독자 경로 - CountDownLatch로 대기
     * 발행자가 Pub/Sub 이벤트를 발행하면 ChemistryEventSubscriber가 releaseWaiters()를 호출
     */
    private String waitAsSubscriber(ChemistryCacheId cacheId, Long userId, Long reportId) {
        CountDownLatch latch = getLatchForWaiter(cacheId, userId);
        if (latch == null) {
            // 등록된 대기자를 못 찾은 경우 (극히 드문 타이밍 이슈) - 캐시 직접 조회로 폴백
            log.warn("대기자 latch 없음. 캐시 직접 조회로 폴백. userId={}, cacheId={}", userId, cacheId);
            return chemistryCacheRepository.findById(cacheId)
                    .map(ChemistryCache::getReport)
                    .orElse(null);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("구독자 대기 중 인터럽트. userId=" + userId);
        }

        // latch 해제 후 캐시에서 report 읽기
        return chemistryCacheRepository.findById(cacheId)
                .map(ChemistryCache::getReport)
                .orElseThrow(() -> new IllegalStateException("latch 해제 후 캐시 report 없음: " + cacheId));
    }

    /**
     * 대기자 맵에 등록. acquireLockAndDecideRole() 내(락 트랜잭션 내)에서 호출
     */
    private void registerWaiter(ChemistryCacheId cacheId, Long userId, Long reportId) {
        waitingMap.computeIfAbsent(cacheId, k -> new CopyOnWriteArrayList<>())
                .add(new WaitingEntry(userId, reportId, new CountDownLatch(1)));
    }

    /**
     * 등록된 대기자의 latch 반환
     */
    private CountDownLatch getLatchForWaiter(ChemistryCacheId cacheId, Long userId) {
        List<WaitingEntry> entries = waitingMap.get(cacheId);
        if (entries == null) return null;
        return entries.stream()
                .filter(e -> e.userId().equals(userId))
                .map(WaitingEntry::latch)
                .findFirst()
                .orElse(null);
    }

    /**
     * Pub/Sub 수신 후 ChemistryEventSubscriber가 호출
     * 해당 버킷 조합의 모든 대기자 latch를 해제하고 맵에서 제거
     *
     * @return 알림 대상 (userId, reportId) 목록 - SSE push에 사용
     */
    public List<WaitingEntry> releaseWaiters(ChemistryCacheId cacheId) {
        List<WaitingEntry> entries = waitingMap.remove(cacheId);
        if (entries == null) return List.of();
        entries.forEach(e -> e.latch().countDown());
        return entries;
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

    /**
     * 대기자 정보 record. ChemistryEventSubscriber에서 SSE push 대상 식별에 사용
     */
    public record WaitingEntry(Long userId, Long reportId, CountDownLatch latch) {}
}
