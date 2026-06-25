package com.mycpt.backend.domain.chemistry.repository;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.JpaTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Sql(scripts = "/sql/chemistry_cache_seed.sql")
@DisplayName("ChemistryCache/ChemistryReport Repository 슬라이스 테스트")
public class ChemistryRepositoryTest extends JpaTestSupport {

    @Autowired ChemistryCacheRepository chemistryCacheRepository;
    @Autowired ChemistryReportRepository chemistryReportRepository;
    @Autowired UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        chemistryReportRepository.deleteAll();
        userA = userRepository.save(User.create("kakao-a", "유저A", null));
        userB = userRepository.save(User.create("kakao-b", "유저B", null));
        userC = userRepository.save(User.create("kakao-c", "유저C", null));
    }

    // 기본 cacheId - seeding으로 존재가 보장된 버킷 조합
    private static final ChemistryCacheId DEFAULT_CACHE_ID =
            new ChemistryCacheId(1, 1, 1, 1, 1, 1, 1, 1);

    private ChemistryReport saveReport(User requester, User partner, ChemistryReportStatus status) {
        ChemistryReport report = ChemistryReport.create(
                requester, partner, TestType.DISC, DEFAULT_CACHE_ID
        );
        if (status == ChemistryReportStatus.READY) report.complete(DEFAULT_CACHE_ID);
        if (status == ChemistryReportStatus.ERROR) report.fail();
        return chemistryReportRepository.save(report);
    }

    // ── ChemistryCacheRepository ─────────────────────────────────────────────

    @Nested
    @DisplayName("ChemistryCacheRepository.findByIdWithLock()")
    class FindByIdWithLock {

        @Test
        @DisplayName("[ST-ChemistryCacheRepo-락-획득]")
        void 락_획득() {
            // given - seeding으로 status=NULL 행 존재 보장

            // when
            Optional<ChemistryCache> result =
                    chemistryCacheRepository.findByIdWithLock(DEFAULT_CACHE_ID);

            // then: status=NULL 행 PESSIMISTIC 락 획득, status=NULL 반환 값 확인
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(ChemistryCacheStatus.NULL);
        }
    }

    // ── ChemistryReportRepository ────────────────────────────────────────────

    @Nested
    @DisplayName("ChemistryReportRepository.findByUserIdWithCursor()")
    class FindByUserIdWithCursor {

        @Test
        @DisplayName("[ST-ChemistryReportRepo-커서-ERROR필터]")
        void 커서_ERROR필터() {
            // given
            saveReport(userA, userB, ChemistryReportStatus.READY);
            saveReport(userA, userB, ChemistryReportStatus.ERROR);

            // when
            List<ChemistryReport> result = chemistryReportRepository.findByUserIdWithCursor(
                    userA.getId(), null, ChemistryReportStatus.ERROR, null, PageRequest.of(0, 10)
            );

            // then: ERROR 제외, READY만 응답
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(ChemistryReportStatus.READY);
        }

        @Test
        @DisplayName("[ST-ChemistryReportRepo-커서-partnerId필터]")
        void 커서_partnerId필터() {
            // given
            saveReport(userA, userB, ChemistryReportStatus.READY);
            saveReport(userA, userC, ChemistryReportStatus.READY);

            // when
            List<ChemistryReport> result = chemistryReportRepository.findByUserIdWithCursor(
                    userA.getId(), userB.getId(), ChemistryReportStatus.ERROR, null, PageRequest.of(0, 10)
            );

            // then: partnerId=B 필터 시 B 관련만 반환
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPartner().getId()).isEqualTo(userB.getId());
        }

        @Test
        @DisplayName("[ST-ChemistryReportRepo-커서-페이지네이션]")
        void 커서_페이지네이션() {
            // given
            saveReport(userA, userB, ChemistryReportStatus.READY);
            saveReport(userA, userB, ChemistryReportStatus.READY);
            ChemistryReport third = saveReport(userA, userB, ChemistryReportStatus.READY);

            // when
            List<ChemistryReport> result = chemistryReportRepository.findByUserIdWithCursor(
                    userA.getId(), null, ChemistryReportStatus.ERROR, third.getId(), PageRequest.of(0, 10)
            );

            // then: id < cursor 인 행만 반환
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.getId() < third.getId());
        }
    }
}
