package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.common.llm.AnthropicLlmClient;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.chemistry.repository.ChemistryCacheRepository;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.notification.service.SseService;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@Sql(scripts = "/sql/chemistry_cache_seed.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("ChemistryCacheService 동시성 부하 통합 테스트")
public class ChemistryCacheConcurrencyLoadTest extends IntegrationTestSupport {

    @MockitoBean
    AnthropicLlmClient llmClient;

    @MockitoSpyBean
    SseService sseService;

    @Autowired ChemistryReportProcessor chemistryReportProcessor;
    @Autowired ChemistryReportRepository chemistryReportRepository;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() {
        given(llmClient.complete(any())).willReturn("## 생성된 케미 보고서");
    }

    private User stubUser(String suffix) {
        return userRepository.save(User.create("kakao-" + suffix, suffix, null));
    }

    private LatestBuckets stubBuckets(int d, int i, int s, int c) {
        return new LatestBuckets(d, i, s, c);
    }

    private ChemistryReport createChemistryReport(User requester, User partner, ChemistryCacheId cacheId) {
        return chemistryReportRepository.save(
                ChemistryReport.create(requester, partner, TestType.DISC, cacheId));
    }

    private ChemistryCacheId cacheIdForRepetition(int repetition) {
        int n = repetition - 1;
        int d = (n % 3) + 1;
        int i = ((n / 3) % 3) + 1;
        int s = ((n / 9) % 3) + 1;
        int c = ((n / 27) % 3) + 1;
        return new ChemistryCacheId(d, i, s, c, 1, 1, 1, 1);
    }

    @RepeatedTest(value = 30, name = "{currentRepetition}/{totalRepetitions}회차")
    @DisplayName("[IT-ChemistryCacheSvc-동시요청부하-LLM단일호출]")
    void 동시요청부하_LLM단일호출(RepetitionInfo repetitionInfo) throws Exception {
        // given: 회차마다 독립된 버킷 조합 - 트라이얼 간 상태 간섭 방지
        ChemistryCacheId cacheId = cacheIdForRepetition(repetitionInfo.getCurrentRepetition());
        clearInvocations(llmClient);    // 회차별 verify 정확성 보장 (프레임워크 기본 리셋에 기대지 않고 명시)

        given(llmClient.complete(any())).willAnswer(inv -> {
            Thread.sleep(300);
            return "## 생성된 케미 보고서";
        });

        int threadCount = 5;
        List<ChemistryReport> reports = new ArrayList<>();
        for (int i = 0; i < threadCount; ++i) {
            reports.add(createChemistryReport(
                    stubUser("load-" + repetitionInfo.getCurrentRepetition() + "-r" + i),
                    stubUser("load-" + repetitionInfo.getCurrentRepetition() + "-p" + i),
                    cacheId
            ));
        }

        LatestBuckets rb = stubBuckets(cacheId.getRequesterD(), cacheId.getRequesterI(), cacheId.getRequesterS(), cacheId.getRequesterC());
        LatestBuckets pb = stubBuckets(cacheId.getPartnerD(), cacheId.getPartnerI(), cacheId.getPartnerS(), cacheId.getPartnerC());

        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // when: 모든 스레드가 동시에 출발하도록 동기화
        for (ChemistryReport report : reports) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    chemistryReportProcessor.process(report.getId(), rb, pb);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        startLatch.countDown();
        for (Future<?> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        executor.shutdown();

        // then: 스레드 수와 무관하게 LLM은 정확히 1회만 호출됨
        verify(llmClient, times(1)).complete(any());
        for (ChemistryReport report : reports) {
            assertThat(chemistryReportRepository.findById(report.getId())
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryReportStatus.READY);
        }
    }
}
