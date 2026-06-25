package com.mycpt.backend.domain.chemistry.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ChemistryCacheId 단위 테스트")
class ChemistryCacheIdTest {

    @Nested
    @DisplayName("equals() / hashCode()")
    class Equality {

        @Test
        @DisplayName("[UT-ChemistryCacheId-동등성-동일버킷]")
        void 동등성_동일버킷() {
            // given & when
            ChemistryCacheId a = new ChemistryCacheId(1, 2, 1, 3, 2, 1, 3, 1);
            ChemistryCacheId b = new ChemistryCacheId(1, 2, 1, 3, 2, 1, 3, 1);

            // then: 8축 값 동일 시 equals/hashCode 동등
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("[UT-ChemistryCacheId-동등성-다른버킷]")
        void 동등성_다른버킷() {
            // given & when
            ChemistryCacheId ab = new ChemistryCacheId(1, 2, 1, 3, 2, 1, 3, 1);
            ChemistryCacheId ba = new ChemistryCacheId(2, 1, 3, 1, 1, 2, 1, 3);

            // then
            assertThat(ab).isNotEqualTo(ba);
        }
    }
}