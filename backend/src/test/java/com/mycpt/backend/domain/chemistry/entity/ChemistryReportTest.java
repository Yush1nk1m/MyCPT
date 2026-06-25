package com.mycpt.backend.domain.chemistry.entity;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("ChemistryReport 엔티티 단위 테스트")
class ChemistryReportTest {

    private User stubUser(Long id) {
        return setId(User.create("kakao-" + id, "유저" + id, null), id);
    }

    private ChemistryCacheId stubCacheId() {
        return new ChemistryCacheId(1, 2, 1, 3, 2, 1, 3, 1);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("[UT-ChemistryReport-상태전이-create]")
        void create() {
            // given
            User requester = stubUser(1L);
            User partner = stubUser(2L);
            ChemistryCacheId cacheId = stubCacheId();

            // when
            ChemistryReport report = ChemistryReport.create(requester, partner, TestType.DISC, cacheId);

            // then: status=GENERATING, cacheId 생성 시점 세팅, createdAt 세팅
            assertThat(report.getStatus()).isEqualTo(ChemistryReportStatus.GENERATING);
            assertThat(report.getCacheId()).isEqualTo(cacheId);
            assertThat(report.getCreatedAt()).isNotNull();
            assertThat(report.getRequester()).isEqualTo(requester);
            assertThat(report.getPartner()).isEqualTo(partner);
        }
    }

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("[UT-ChemistryReport-상태전이-complete]")
        void complete() {
            // given
            ChemistryCacheId cacheId = stubCacheId();
            ChemistryReport report = ChemistryReport.create(
                    stubUser(1L), stubUser(2L), TestType.DISC, cacheId
            );

            // when
            report.complete(cacheId);

            // then: status=READY 전이
            assertThat(report.getStatus()).isEqualTo(ChemistryReportStatus.READY);
        }
    }

    @Nested
    @DisplayName("fail()")
    class Fail {

        @Test
        @DisplayName("[UT-ChemistryReport-상태전이-fail]")
        void fail() {
            // given
            ChemistryReport report = ChemistryReport.create(
                    stubUser(1L), stubUser(2L), TestType.DISC, stubCacheId()
            );

            // when
            report.fail();

            // then: status=ERROR 전이
            assertThat(report.getStatus()).isEqualTo(ChemistryReportStatus.ERROR);
        }
    }
}