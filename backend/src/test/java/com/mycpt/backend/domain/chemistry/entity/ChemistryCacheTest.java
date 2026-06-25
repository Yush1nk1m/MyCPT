package com.mycpt.backend.domain.chemistry.entity;

import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("ChemistryCache 엔티티 단위 테스트")
class ChemistryCacheTest {

    // seeding으로 생성된 행을 리플렉션으로 재현
    private ChemistryCache stubNullCache() {
        ChemistryCache cache = new ChemistryCache();
        setField(cache, "id", new ChemistryCacheId(1, 2, 1, 3, 2, 1, 3, 1));
        setField(cache, "status", ChemistryCacheStatus.NULL);
        setField(cache, "report", null);
        setField(cache, "createdAt", null);
        return cache;
    }

    @Nested
    @DisplayName("startGenerating()")
    class StartGenerating {

        @Test
        @DisplayName("[UT-ChemistryCache-상태전이-startGenerating]")
        void 상태전이_startGenerating() {
            // given
            ChemistryCache cache = stubNullCache();

            // when
            cache.startGenerating();

            // then: status=GENERATING
            assertThat(cache.getStatus()).isEqualTo(ChemistryCacheStatus.GENERATING);
        }
    }

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("[UT-ChemistryCache-상태전이-complete]")
        void 상태전이_complete(){
            // given
            ChemistryCache cache = stubNullCache();
            cache.startGenerating();
            String report = "## 케미 보고서";
            LocalDateTime now = LocalDateTime.now();

            // when
            cache.complete(report, now);

            // then: status=READY, report/createdAt 세팅
            assertThat(cache.getStatus()).isEqualTo(ChemistryCacheStatus.READY);
            assertThat(cache.getReport()).isEqualTo(report);
            assertThat(cache.getCreatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("[UT-ChemistryCache-상태전이-refresh]")
        void 상태전이_refresh() {
            // given
            ChemistryCache cache = stubNullCache();
            cache.complete("기존 보고서", LocalDateTime.now().minusDays(366));

            // when
            cache.refresh();

            // then: status=GENERATING 리셋, report=null, createdAt=null
            assertThat(cache.getStatus()).isEqualTo(ChemistryCacheStatus.GENERATING);
            assertThat(cache.getReport()).isNull();
            assertThat(cache.getCreatedAt()).isNull();
        }
    }
}