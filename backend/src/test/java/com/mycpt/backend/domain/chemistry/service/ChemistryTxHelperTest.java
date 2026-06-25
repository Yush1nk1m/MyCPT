package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.enums.TestType;
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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;

/**
 * ChemistryTxHelper 트랜잭션 격리 검증.
 *
 * 검증 핵심: @Transactional로 외부 트랜잭션(TX-A)을 만들고,
 * ChemistryTxHelper 메서드(REQUIRES_NEW TX-B)가 TX-A와 독립적으로 커밋되는지
 * 별도 스레드(별도 커넥션)에서 조회해 확인한다.
 *
 * REQUIRES_NEW가 아니었다면:
 *   TX-B 없이 TX-A에 참여 → TX-A 커밋 전 별도 커넥션에서 변경 불가시 → 테스트 실패
 *
 * @AfterEach로 REQUIRES_NEW TX-B가 커밋한 chemistry_cache 변경을 초기화.
 * (TX-A 롤백으로 chemistry_reports는 자동 정리되나 chemistry_cache는 커밋됐으므로 수동 정리)
 */
@Sql(scripts = "/sql/chemistry_cache_seed.sql")
@DisplayName("ChemistryTxHelper 트랜잭션 격리 통합 테스트")
class ChemistryTxHelperTest extends IntegrationTestSupport {

    @Autowired ChemistryTxHelper chemistryTxHelper;
    @Autowired ChemistryCacheRepository chemistryCacheRepository;
    @Autowired ChemistryReportRepository chemistryReportRepository;
    @Autowired UserRepository userRepository;
    @Autowired TransactionTemplate transactionTemplate;

    // 각 테스트가 독립된 버킷 조합을 사용해 상태 간섭 방지
    private static final ChemistryCacheId LOCK_CACHE_ID =
            new ChemistryCacheId(1, 1, 1, 1, 1, 1, 1, 2);
    private static final ChemistryCacheId SAVE_CACHE_ID =
            new ChemistryCacheId(1, 1, 1, 1, 1, 1, 2, 1);
    private static final ChemistryCacheId REPORT_CACHE_ID =
            new ChemistryCacheId(1, 1, 1, 1, 1, 1, 2, 2);

    private User requester;
    private User partner;

    @BeforeEach
    void setUp() {
        chemistryReportRepository.deleteAll();
        userRepository.deleteAll();
        requester = userRepository.save(User.create("kakao-tx-1", "발행자", null));
        partner = userRepository.save(User.create("kakao-tx-2", "파트너", null));
    }

    // ── acquireLockAndDecideRole() ────────────────────────────────────────────

    @Nested
    @DisplayName("acquireLockAndDecideRole()")
    class AcquireLockAndDecideRole {

        @Test
        @DisplayName("[IT-ChemistryTxHelper-트랜잭션격리-발행자GENERATING즉시반영]")
        void 트랜잭션격리_발행자GENERATING즉시반영() {
            // given - status=NULL (seeding 초기 상태) 확인
            assertThat(chemistryCacheRepository.findById(LOCK_CACHE_ID)
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryCacheStatus.NULL);

            // when - 외부 트랜잭션 내에서 acquireLockAndDecideRole() 호출 후 강제 롤백
            assertThatThrownBy(() ->
                    transactionTemplate.execute(status -> {
                        // 외부 트랜잭션 시작됨
                        // REQUIRES_NEW -> 별도 커넥션에서 즉시 커밋 (외부 트랜잭션과 독립)
                        ChemistryCacheStatus returned = chemistryTxHelper.acquireLockAndDecideRole(
                                LOCK_CACHE_ID, 365L, () -> {}
                        );

                        assertThat(returned).isEqualTo(ChemistryCacheStatus.NULL);

                        // 외부 트랜잭션 강제 롤백
                        throw new RuntimeException("외부 트랜잭션 강제 롤백");
                    })
            ).isInstanceOf(RuntimeException.class);

            // then - 외부 롤백 후 DB 조회
            // REQUIRES_NEW가 정상 작동했다면 내부 커밋이 살아 있어 GENERATING 조회됨
            // 자가 호출이었다면 외부 롤백과 함께 사라져 NULL 조회됨
            assertThat(chemistryCacheRepository.findById(LOCK_CACHE_ID)
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryCacheStatus.GENERATING);
        }
    }

    @Nested
    @DisplayName("saveCompletedCache()")
    class SaveCompletedCache {

        @Test
        @DisplayName("[IT-ChemistryTxHelper-트랜잭션격리-완료캐시READY즉시반영]")
        void 트랜잭션격리_완료캐시READY즉시반영() {
            // given - GENERATING 상태로 세팅
            chemistryTxHelper.acquireLockAndDecideRole(SAVE_CACHE_ID, 365L, () -> {});
            assertThat(chemistryCacheRepository.findById(SAVE_CACHE_ID)
                    .orElseThrow().getStatus())
                    .isEqualTo(ChemistryCacheStatus.GENERATING);

            // when
            assertThatThrownBy(() ->
                    transactionTemplate.execute(status -> {
                        chemistryTxHelper.saveCompletedCache(SAVE_CACHE_ID, "## 케미 보고서");
                        throw new RuntimeException("외부 트랜잭션 강제 롤백");
                    })
            ).isInstanceOf(RuntimeException.class);

            // then - 외부 롤백 후에도 READY + report 유지
            ChemistryCache saved = chemistryCacheRepository.findById(SAVE_CACHE_ID).orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(ChemistryCacheStatus.READY);
            assertThat(saved.getReport()).isEqualTo("## 케미 보고서");
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("completeReport()")
    class CompleteReport {

        @Test
        @DisplayName("[IT-ChemistryTxHelper-트랜잭션격리-완료보고서READY즉시반영]")
        void 트랜잭션격리_완료보고서READY즉시반영() {
            // given - GENERATING 상태 보고서 삽
            ChemistryReport report = chemistryReportRepository.save(
                    ChemistryReport.create(requester, partner, TestType.DISC, REPORT_CACHE_ID)
            );
            assertThat(report.getStatus()).isEqualTo(ChemistryReportStatus.GENERATING);

            // when - 외부 트랜잭션 내에서 completeReport() 호출 후 강제 롤백
            assertThatThrownBy(() ->
                    transactionTemplate.execute(status -> {
                        chemistryTxHelper.completeReport(report.getId(), REPORT_CACHE_ID);
                        throw new RuntimeException("외부 트랜잭션 강제 롤백");
                    })
            ).isInstanceOf(RuntimeException.class);

            // then - 외부 롤백 후에도 chemistry_reports.status=READY 유지
            // 자가 호출이었다면 외부 롤백과 함께 GENERATING으로 복원됨
            ChemistryReport updated = chemistryReportRepository.findById(report.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(ChemistryReportStatus.READY);
            assertThat(updated.getCacheId()).isEqualTo(REPORT_CACHE_ID);
        }
    }
}