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
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ChemistryCacheService {

    private final ChemistryCacheRepository chemistryCacheRepository;
    private final AnthropicLlmClient llmClient;
    private final ChemistryEventPublisher eventPublisher;
    private final ChemistryTxHelper txHelper;
    private final long ttlDays;

    private final Map<ChemistryCacheId, List<WaitingEntry>> waitingMap = new ConcurrentHashMap<>();
    private final long subscriberWaitTimeoutSeconds;

    public ChemistryCacheService(
            ChemistryCacheRepository chemistryCacheRepository,
            AnthropicLlmClient llmClient,
            ChemistryEventPublisher eventPublisher,
            ChemistryTxHelper txHelper,
            @Value("${cache.chemistry.ttl-days:365}") long ttlDays,
            @Value("${chemistry.subscriber-wait-timeout-seconds:300}") long subscriberWaitTimeoutSeconds
    ) {
        this.chemistryCacheRepository = chemistryCacheRepository;
        this.llmClient = llmClient;
        this.eventPublisher = eventPublisher;
        this.txHelper = txHelper;
        this.ttlDays = ttlDays;
        this.subscriberWaitTimeoutSeconds = subscriberWaitTimeoutSeconds;
    }

    public String getOrGenerate(
            ChemistryCacheId cacheId,
            Long userId,
            Long reportId,
            LatestBuckets requesterBuckets,
            LatestBuckets partnerBuckets
    ) {
        throw new UnsupportedOperationException("stub");
    }

    private String generateAsPublisher(
            ChemistryCacheId cacheId,
            LatestBuckets requesterBuckets,
            LatestBuckets partnerBuckets
    ) {
        throw new UnsupportedOperationException("stub");
    }

    private String waitAsSubscriber(ChemistryCacheId cacheId, Long userId, Long reportId) {
        throw new UnsupportedOperationException("stub");
    }

    private void registerWaiter(ChemistryCacheId cacheId, Long userId, Long reportId) {
        throw new UnsupportedOperationException("stub");
    }

    private CountDownLatch getLatchForWaiter(ChemistryCacheId cacheId, Long userId) {
        throw new UnsupportedOperationException("stub");
    }

    public List<WaitingEntry> releaseWaiters(ChemistryCacheId cacheId) {
        throw new UnsupportedOperationException("stub");
    }

    private void removeWaiter(ChemistryCacheId cacheId, Long userId) {
        throw new UnsupportedOperationException("stub");
    }

    String buildPrompt(LatestBuckets a, LatestBuckets b) {
        throw new UnsupportedOperationException("stub");
    }

    public record WaitingEntry(Long userId, Long reportId, CountDownLatch latch) {}
}
