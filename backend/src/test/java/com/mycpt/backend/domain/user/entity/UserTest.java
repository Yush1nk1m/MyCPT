package com.mycpt.backend.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

    @Nested
    @DisplayName("withdraw()")
    class Withdraw {

        @Test
        @DisplayName("[UT-User-탈퇴-익명화]")
        void 탈퇴_익명화() {
            // given
            User user = User.create("kakao-1", "유신", "https://image.url");

            // when
            user.withdraw();

            // then
            assertThat(user.getKakaoId()).isNull();
            assertThat(user.getBirthYear()).isNull();
            assertThat(user.getGender()).isNull();
            assertThat(user.getNickname()).isEqualTo("유신");
            assertThat(user.getProfileImageUrl()).isEqualTo("https://image.url");
            assertThat(user.getDeletedAt()).isNotNull();
        }
    }
}