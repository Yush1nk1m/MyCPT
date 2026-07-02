package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.common.llm.AnthropicLlmClient;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.chemistry.repository.ChemistryCacheRepository;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@Sql(scripts = "/sql/chemistry_cache_seed.sql")
@DisplayName("ChemistryCacheRecoveryScheduler 통합 테스트")
class ChemistryCacheRecoverySchedulerTest extends IntegrationTestSupport {

    @MockitoBean
    AnthropicLlmClient llmClient;

    @Autowired
    ChemistryCacheRecoveryScheduler recoveryScheduler;
    @Autowired
    ChemistryCacheRepository chemistryCacheRepository;
    @Autowired
    ChemistryReportRepository chemistryReportRepository;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        given(llmClient.complete(any())).willReturn("## 생성된 케미 보고서");
    }

    private User stubUser(String suffix) {
        return userRepository.save(User.create("kakao-rcv-" + suffix, suffix, null));
    }

    private ChemistryReport createChemistryReport(User requester, User partner, ChemistryCacheId cacheId) {
        return chemistryReportRepository.save(
                ChemistryReport.create(requester, partner, TestType.DISC, cacheId)
        );
    }

    private ChemistryCache setCacheAsStaleGenerating(ChemistryCacheId id, LocalDateTime updatedAt) {
        ChemistryCache cache = chemistryCacheRepository.findById(id).orElseThrow();
        cache.startGenerating();
        setField(cache, "updatedAt", updatedAt);
        return chemistryCacheRepository.save(cache);
    }

    @Nested
    @DisplayName("recoverStaleGeneratingCaches()")
    class RecoverStaleGeneratingCaches {

        @Test
        @DisplayName("[IT-ChemistryCacheRecoveryScheduler-스테일복구-READY전이및ERROR보고서교정]")
        void 스테일복구_ERROR전이및ERROR보고서교정() {
            // given: GENERATING + updated_at 11분 전
            ChemistryCacheId cacheId = new ChemistryCacheId(1, 3, 2, 1, 3, 1, 2, 3);
            setCacheAsStaleGenerating(cacheId, LocalDateTime.now().minusMinutes(11));

            // 같은 cacheId로 이미 ERROR 처리된 보고서 (조용한 교정 대상)
            ChemistryReport errorReport = createChemistryReport(stubUser("err-r"), stubUser("err-p"), cacheId);
            setField(errorReport, "status", ChemistryReportStatus.ERROR);
            chemistryReportRepository.save(errorReport);

            // when
            recoveryScheduler.recoverStaleGeneratingCaches();

            // then
            ChemistryCache recovered = chemistryCacheRepository.findById(cacheId).orElseThrow();
            assertThat(recovered.getStatus()).isEqualTo(ChemistryCacheStatus.READY);
            assertThat(recovered.getReport()).isEqualTo("## 생성된 케미 보고서");

            assertThat(chemistryReportRepository.findById(errorReport.getId())
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryReportStatus.READY);

            verify(llmClient, times(1)).complete(any());
        }

        @Test
        @DisplayName("[IT-ChemistryCacheRecoveryScheduler-스테일복구-임계값미달]")
        void 스테일복구_임계값미달() {
            // given: 방금 GENERATING 진입 (updated_at = now)
            ChemistryCacheId cacheId = new ChemistryCacheId(1, 3, 2, 2, 3, 1, 2, 1);
            ChemistryCache fresh = chemistryCacheRepository.findById(cacheId).orElseThrow();
            fresh.startGenerating();
            chemistryCacheRepository.save(fresh);

            // when
            recoveryScheduler.recoverStaleGeneratingCaches();

            // then
            assertThat(chemistryCacheRepository.findById(cacheId)
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryCacheStatus.GENERATING);
            verify(llmClient, times(0)).complete(any());
        }

        @Test
        @DisplayName("[IT-ChemistryCacheRecoveryScheduler-스테일복구-재시도중상태유지]")
        void 스테일복구_재시도중상태유지() {
            // given
            ChemistryCacheId cacheId = new ChemistryCacheId(2, 3, 1, 2, 1, 3, 2, 1);
            LocalDateTime staleTime = LocalDateTime.now().minusMinutes(15);
            setCacheAsStaleGenerating(cacheId, staleTime);

            // LLM 호출 중(=claimForRetry 커밋 직후) 캐시 상태 관찰
            given(llmClient.complete(any())).willAnswer(inv -> {
                ChemistryCache midway = chemistryCacheRepository.findById(cacheId).orElseThrow();
                assertThat(midway.getStatus()).isEqualTo(ChemistryCacheStatus.GENERATING);
                assertThat(midway.getUpdatedAt()).isAfter(staleTime);
                return "## 생성된 케미 보고서";
            });

            // when
            recoveryScheduler.recoverStaleGeneratingCaches();

            // then
            assertThat(chemistryCacheRepository.findById(cacheId)
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryCacheStatus.READY);
        }
    }
}