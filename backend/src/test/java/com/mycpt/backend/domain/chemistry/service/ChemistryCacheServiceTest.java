package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.common.llm.AnthropicLlmClient;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.chemistry.event.ChemistryReportIssuedEvent;
import com.mycpt.backend.domain.chemistry.repository.ChemistryCacheRepository;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.colleague.repository.ColleagueRepository;
import com.mycpt.backend.domain.notification.repository.NotificationRepository;
import com.mycpt.backend.domain.notification.service.SseService;
import com.mycpt.backend.domain.result.entity.DiscTest;
import com.mycpt.backend.domain.result.repository.DiscTestRepository;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@Sql(scripts = "/sql/chemistry_cache_seed.sql")
@TestPropertySource(properties = "chemistry.subscriber-wait-timeout-seconds=2")
@DisplayName("ChemistryCacheService 통합 테스트")
public class ChemistryCacheServiceIntegrationTest extends IntegrationTestSupport {

    @MockitoBean
    AnthropicLlmClient llmClient;

    @Autowired ChemistryService chemistryService;
    @Autowired ChemistryReportProcessor chemistryReportProcessor;
    @Autowired ChemistryCacheRepository chemistryCacheRepository;
    @Autowired ChemistryReportRepository chemistryReportRepository;
    @Autowired UserRepository userRepository;
    @Autowired ColleagueRepository colleagueRepository;
    @Autowired DiscTestRepository discTestRepository;

    @BeforeEach
    void setUp() {
        given(llmClient.complete(any())).willReturn("## 생성된 케미 보고서");
    }

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────────

    private User stubUser(String suffix) {
        return userRepository.save(User.create("kakao-" + suffix, suffix, null));
    }

    private void createColleague(User a, User b) {
        if (a.getId() > b.getId()) {
            User temp = a;
            a = b;
            b = temp;
        }
        colleagueRepository.save(Colleague.create(a, b));
    }

    private ChemistryReport createChemistryReport(User requester, User partner, ChemistryCacheId cacheId) {
        return chemistryReportRepository.save(
                ChemistryReport.create(requester, partner, TestType.DISC, cacheId)
        );
    }

    private LatestBuckets stubBuckets(int d, int i, int s, int c) {
        return new LatestBuckets(d, i, s, c);
    }

    private void setCacheAsReady(ChemistryCacheId id, LocalDateTime createdAt) {
        ChemistryCache cache = chemistryCacheRepository.findById(id).orElseThrow();
        setField(cache, "status", ChemistryCacheStatus.READY);
        setField(cache, "report", "기존 보고서");
        setField(cache, "createdAt", createdAt);
        chemistryCacheRepository.save(cache);
    }

    private void awaitUntil(Duration timeout, BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) return;
            sleepQuietly(100);
        }
        throw new AssertionError("조건이 " + timeout + " 내 충족되지 않음");
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ── Lazy Caching ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Lazy Caching")
    class LazyCaching {

        @Test
        @DisplayName("[IT-ChemistryCacheSvc-캐시미스-LLM호출후READY]")
        void 캐시미스_LLM호출후READY() {
            // given
            ChemistryCacheId cacheId = new ChemistryCacheId(1, 2, 1, 3, 2, 1, 3, 1);
            ChemistryReport report = createChemistryReport(stubUser("miss-r"), stubUser("miss-p"), cacheId);

            // when
            chemistryReportProcessor.process(
                    report.getId(),
                    stubBuckets(1, 2, 1, 3),
                    stubBuckets(2, 1, 3, 1)
            );

            // then
            ChemistryCache cache = chemistryCacheRepository.findById(cacheId).orElseThrow();
            assertThat(cache.getStatus()).isEqualTo(ChemistryCacheStatus.READY);
            assertThat(cache.getReport()).isEqualTo("## 생성된 케미 보고서");

            assertThat(chemistryReportRepository.findById(report.getId())
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryReportStatus.READY);

            verify(llmClient, times(1)).complete(any());
        }

        @Test
        @DisplayName("[IT-ChemistryCacheSvc-캐시히트유효-LLM미호출]")
        void 캐시히트유효_LLM미호출() {
            // given
            ChemistryCacheId cacheId = new ChemistryCacheId(1, 1, 1, 1, 1, 1, 1, 2);
            setCacheAsReady(cacheId, LocalDateTime.now().minusDays(10));
            ChemistryReport report = createChemistryReport(stubUser("hit-r"), stubUser("hit-p"), cacheId);

            // when
            chemistryReportProcessor.process(
                    report.getId(),
                    stubBuckets(1, 1, 1, 1),
                    stubBuckets(1, 1, 1, 2)
            );

            // then
            verify(llmClient, times(0)).complete(any());
            assertThat(chemistryReportRepository.findById(report.getId())
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryReportStatus.READY);
        }

        @Test
        @DisplayName("[IT-ChemistryCacheSvc-캐시만료-LLM재호출]")
        void 캐시만료_LLM재호출() {
            // given
            ChemistryCacheId cacheId = new ChemistryCacheId(1, 1, 2, 1, 1, 1, 2, 2);
            setCacheAsReady(cacheId, LocalDateTime.now().minusDays(366));
            ChemistryReport report = createChemistryReport(stubUser("exp-r"), stubUser("exp-p"), cacheId);

            // when
            chemistryReportProcessor.process(
                    report.getId(),
                    stubBuckets(1, 1, 2, 1),
                    stubBuckets(1, 1, 2, 2)
            );

            // then
            ChemistryCache refreshed = chemistryCacheRepository.findById(cacheId).orElseThrow();
            assertThat(refreshed.getStatus()).isEqualTo(ChemistryCacheStatus.READY);
            assertThat(refreshed.getReport()).isEqualTo("## 생성된 케미 보고서");
            assertThat(refreshed.getCreatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
            verify(llmClient, times(1)).complete(any());
        }
    }

    // ── Duplication Defense ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Duplication Defense")
    class DuplicationDefense {

        @Test
        @DisplayName("[IT-ChemistryCacheSvc-동시요청2개-LLM단일호출]")
        void 동시요청3개_LLM단일호출() throws Exception {
            // given
            ChemistryCacheId sharedCacheId = new ChemistryCacheId(1, 2, 1, 3, 2, 1, 3, 2);
            ChemistryReport r1 = createChemistryReport(stubUser("tri-a"), stubUser("tri-b"), sharedCacheId);
            ChemistryReport r2 = createChemistryReport(stubUser("tri-c"), stubUser("tri-d"), sharedCacheId);
            ChemistryReport r3 = createChemistryReport(stubUser("tri-e"), stubUser("tri-f"), sharedCacheId);

            given(llmClient.complete(any())).willAnswer(inv -> {
                Thread.sleep(300);
                return "## 생성된 케미 보고서";
            });

            LatestBuckets rb = stubBuckets(2, 1, 2, 1);
            LatestBuckets pb = stubBuckets(1, 2, 1, 2);
            CountDownLatch startLatch = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(3);

            // when
            Future<?> f1 = executor.submit(() -> {
                try {
                    startLatch.await();
                    chemistryReportProcessor.process(r1.getId(), rb, pb);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Future<?> f2 = executor.submit(() -> {
                try {
                    startLatch.await();
                    chemistryReportProcessor.process(r2.getId(), rb, pb);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Future<?> f3 = executor.submit(() -> {
                try {
                    startLatch.await();
                    chemistryReportProcessor.process(r3.getId(), rb, pb);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            startLatch.countDown();
            f1.get(30, TimeUnit.SECONDS);
            f2.get(30, TimeUnit.SECONDS);
            f3.get(30, TimeUnit.SECONDS);
            executor.shutdown();

            // then
            verify(llmClient, times(1)).complete(any());
            assertThat(chemistryReportRepository.findById(r1.getId())
                    .orElseThrow().getStatus()).isEqualTo(ChemistryReportStatus.READY);
            assertThat(chemistryReportRepository.findById(r2.getId())
                    .orElseThrow().getStatus()).isEqualTo(ChemistryReportStatus.READY);
            assertThat(chemistryReportRepository.findById(r3.getId())
                    .orElseThrow().getStatus()).isEqualTo(ChemistryReportStatus.READY);
        }

        @Test
        @DisplayName("[IT-ChemistryCacheSvc-다른버킷동시요청-LLM각각호출]")
        void 다른버킷동시요청_LLM각각호출() throws Exception {
            // given
            ChemistryCacheId cacheId1 = new ChemistryCacheId(1, 1, 1, 1, 2, 2, 2, 2);
            ChemistryCacheId cacheId2 = new ChemistryCacheId(3, 3, 3, 3, 1, 2, 3, 1);
            ChemistryReport report1 = createChemistryReport(stubUser("diff-a"), stubUser("diff-b"), cacheId1);
            ChemistryReport report2 = createChemistryReport(stubUser("diff-c"), stubUser("diff-d"), cacheId2);

            CountDownLatch startLatch = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(2);

            Future<?> f1 = executor.submit(() -> {
                try {
                    startLatch.await();
                    chemistryReportProcessor.process(
                            report1.getId(),
                            stubBuckets(1, 1, 1, 1),
                            stubBuckets(2, 2, 2, 2)
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Future<?> f2 = executor.submit(() -> {
                try {
                    startLatch.await();
                    chemistryReportProcessor.process(
                            report2.getId(),
                            stubBuckets(3, 3, 3, 3),
                            stubBuckets(1, 2, 3, 1)
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            startLatch.countDown();
            f1.get(30, TimeUnit.SECONDS);
            f2.get(30, TimeUnit.SECONDS);
            executor.shutdown();

            // then
            verify(llmClient, times(2)).complete(any());
        }
    }

    // ── @TransactionalEventListener(AFTER_COMMIT) 검증 ──────────────────────

    @Nested
    @DisplayName("@TransactionalEventListener AFTER_COMMIT 트리거 보장")
    class TransactionalEventListenerGuarantee {

        @Test
        @DisplayName("[IT-CemistryCacheSvc-AFTER_COMMIT-커밋후처리실행]")
        void AFTER_COMMIT_커밋후처리실행() throws Exception {
            // given
            User requester = stubUser("evt-r");
            User partner = stubUser("evt-p");
            createColleague(requester, partner);

            // 코인 세팅 - issue() 내 deduct() 통과 위함
            setField(requester, "coins", 3);
            setField(requester, "nextCoinAt", null);
            userRepository.save(requester);

            // issue() 내 findLatestBuckets() 통과를 위한 DiscTest 사전 삽입
            discTestRepository.save(DiscTest.createForSelf(requester, 0, 0, 0, 0, 1, 2, 1, 3));
            discTestRepository.save(DiscTest.createForSelf(partner, 0, 0, 0, 0, 2, 1, 3, 1));

            // when - ChemistryService.issue() 전체 흐름 실행
            // @TransactionalEventListener(AFTER_COMMIT)이 트랜잭션 커밋 후 handle() 호출
            // handle() 내 @Async가 별도 스레드에서 process() 실행
            chemistryService.issue(requester.getId(), partner.getId());

            // @Async가 완료될 때까지 대기 (최대 10초)
            // process() 완료 시 chemistry_reports.status = READY
            long deadline = System.currentTimeMillis() + 10_000;
            ChemistryReportStatus finalStatus = null;
            while (System.currentTimeMillis() < deadline) {
                finalStatus = chemistryReportRepository
                        .findByRequesterId(requester.getId())
                        .stream().findFirst()
                        .map(ChemistryReport::getStatus)
                        .orElse(null);
                if (finalStatus == ChemistryReportStatus.READY) break;
                Thread.sleep(100);
            }

            // then
            assertThat(finalStatus).isEqualTo(ChemistryReportStatus.READY);
            verify(llmClient, times(1)).complete(any());
        }

        @Test
        @DisplayName("[IT-ChemistryCacheSvc-AFTER_COMMIT-롤백시미실행]")
        void AFTER_COMMIT_롤백시미실행() throws Exception {
            // given
            User requester = stubUser("rollback-r");
            User partner = stubUser("rollback-p");
            createColleague(requester, partner);

            // 코인 0개 - deduct()에서 BusinessException 발생 -> 트랜잭션 롤백
            setField(requester, "coins", 0);
            userRepository.save(requester);

            // when
            try {
                chemistryService.issue(requester.getId(), partner.getId());
            } catch (Exception ignored) {
                // 코인 부족 예외 예상
            }

            // then - 트랜잭션 롤백 -> AFTER_COMMIT 이벤트 미발행 -> process() 미실행 -> LLM 호출 없음
            Thread.sleep(500);
            verify(llmClient, times(0)).complete(any());

            // chemistry_reports 행도 생성되지 않음
            assertThat(chemistryReportRepository
                    .findByRequesterId(requester.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Recover 시그니처 수정 검증")
    class RecoverSignatureFix {

        @Test
        @DisplayName("[IT-ChemistryReportProcessor-recover-이벤트파라미터매칭]")
        void recover_이벤트파라미터매칭() {
            // given
            ChemistryCacheId cacheId = new ChemistryCacheId(3, 3, 3, 1, 3, 3, 3, 2);
            ChemistryReport report = createChemistryReport(stubUser("rec-r"), stubUser("rec-p"), cacheId);
            LatestBuckets rb = stubBuckets(3, 3, 3, 1);
            LatestBuckets pb = stubBuckets(3, 3, 3, 2);

            given(llmClient.complete(any())).willThrow(new RuntimeException("LLM 호출 실패(테스트)"));

            // when: process()가 아닌 handle()을 직접 호출해야 실제 @Retryable/@Recover 프록시를 태움
            chemistryReportProcessor.handle(
                    new ChemistryReportIssuedEvent(report.getId(), rb, pb)
            );

            // then: maxAttempts=3, backoff 2s*2 소진까지 최대 약 6초 -> 넉넉히 15초 대기
            awaitUntil(Duration.ofSeconds(15), () ->
                    chemistryReportRepository.findById(report.getId())
                            .map(r -> r.getStatus() == ChemistryReportStatus.ERROR)
                            .orElse(false));

            verify(llmClient, times(3)).complete(any());
        }
    }

    @Nested
    @DisplayName("Subscriber Timeout")
    @TestPropertySource(properties = "chemistry.subscriber-wait-timeout-seconds=1")
    class SubscriberTimeout {

        @MockitoSpyBean
        SseService sseService;

        @Test
        @DisplayName("[IT-ChemistryCacheSvc-구독자타임아웃-ERROR전이및대기자맵정리]")
        void 구독자타임아웃_ERROR전이및대기자맵정리() {
            // given
            ChemistryCacheId cacheId = new ChemistryCacheId(2, 2, 2, 2, 2, 2, 2, 3);
            ChemistryReport publisherReport = createChemistryReport(stubUser("to-pub"), stubUser("to-partner1"), cacheId);
            ChemistryReport subscriberReport = createChemistryReport(stubUser("to-sub"), stubUser("to-partner2"), cacheId);

            given(llmClient.complete(any())).willAnswer(inv -> {
                Thread.sleep(4000); // 구독자 타임아웃(2초)보다 충분히 길게, 결국은 성공
                return "## 생성된 케미 보고서";
            });

            LatestBuckets rb = stubBuckets(2, 2, 2, 2);
            LatestBuckets pb = stubBuckets(2, 2, 2, 3);

            // when: 발행자가 먼저 락을 잡도록 함
            chemistryReportProcessor.handle(new ChemistryReportIssuedEvent(publisherReport.getId(), rb, pb));

            awaitUntil(Duration.ofSeconds(3), () ->
                    chemistryCacheRepository.findById(cacheId)
                            .map(c -> c.getStatus() == ChemistryCacheStatus.GENERATING)
                            .orElse(false));

            // 구독자 진입 - GENERATING을 보고 대기자로 등록됨
            chemistryReportProcessor.handle(new ChemistryReportIssuedEvent(subscriberReport.getId(), rb, pb));

            // then: 1초 뒤 구독자는 즉시 ERROR (noRetryFor 대상 - 재시도 없음)
            awaitUntil(Duration.ofSeconds(5), () ->
                    chemistryReportRepository.findById(subscriberReport.getId())
                            .map(r -> r.getStatus() == ChemistryReportStatus.ERROR)
                            .orElse(false));

            // 발행자는 그대로 두면 4초 뒤 성공
            awaitUntil(Duration.ofSeconds(8), () ->
                    chemistryReportRepository.findById(publisherReport.getId())
                            .map(r -> r.getStatus() == ChemistryReportStatus.READY)
                            .orElse(false));

            sleepQuietly(500);  // releaseWaiters() 처리 시간 여유

            // 이미 이탈한 구독자에게는 뒤늦은 발행 성공 SSE가 절대 나가지 않아야 함
            verify(sseService, never())
                    .pushChemistryReady(eq(subscriberReport.getRequester().getId()), any());
        }
    }
}
