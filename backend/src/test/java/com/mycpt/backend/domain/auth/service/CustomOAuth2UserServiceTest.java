package com.mycpt.backend.domain.auth.service;

import com.mycpt.backend.domain.auth.dto.KakaoUserInfo;
import com.mycpt.backend.domain.coin.service.CoinService;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Spring Context 없이 Mockito만 사용
// loadUser() 메서드가 내부적으로 super.loadUser() 메서드를 호출하므로 비즈니스 로직이 분리된 findOrCreateUser() 직접 테스트
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2UserService 단위 테스트")
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoinService coinService;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    // 정상적인 카카오 userinfo 응답 구조
    private KakaoUserInfo kakaoUserInfo;

    @BeforeEach
    void setUp() {
        // 카카오로부터 정상적으로 도달한 응답(super.loadUser())
        // 카카오 API 호출 없이 findOrCreateUser() 메서드만 독립적으로 테스트 가능
        kakaoUserInfo = new KakaoUserInfo(Map.of(
                "id", 1234567890L,
                "properties", Map.of(
                        "nickname", "테스트유저",
                        "profile_image", "https://k.kakaocdn.net/test.jpg"
                )
        ));
    }

    @Nested
    @DisplayName("findOrCreateUser()")
    class FindOrCreateUser {

        @Test
        @DisplayName("[UT-OAuth2UserSvc-회원가입-성공]")
        void 회원가입_성공() {
            // given
            // DB에는 해당 kakao_id가 없음
            given(userRepository.findByKakaoId("1234567890"))
                    .willReturn(Optional.empty());
            // save() 호출 시 전달받은 User 객체를 그대로 반환함
            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            User foundUser = customOAuth2UserService.findOrCreateUser(kakaoUserInfo);

            // then
            // 카카오 응답 필드가 User 엔티티에 올바르게 매핑되었는지 검증
            assertThat(foundUser.getKakaoId()).isEqualTo("1234567890");
            assertThat(foundUser.getNickname()).isEqualTo("테스트유저");
            assertThat(foundUser.getProfileImageUrl()).isEqualTo("https://k.kakaocdn.net/test.jpg");

            // 신규 가입 시 코인 3개 초기 지급 검증
            assertThat(foundUser.getCoins()).isEqualTo(3);

            // save()가 정확히 1번, 올바른 인자로 호출되었는지 검증
            verify(userRepository).save(argThat(user ->
                    user.getKakaoId().equals("1234567890") &&
                            user.getNickname().equals("테스트유저") &&
                            user.getCoins() == 3));
        }

        @Test
        @DisplayName("[Ut-OAuth2UserSvc-기존회원로그인-성공")
        void 기존회원로그인_성공() {
            // given
            // DB에 이미 해당 kakao_id가 있음
            User existingUser = User.create(
                    "1234567890",
                    "테스트유저",
                    "https://k.kakaocdn.net/test.jpg");
            given(userRepository.findByKakaoId("1234567890"))
                    .willReturn(Optional.of(existingUser));

            // when
            User foundUser = customOAuth2UserService.findOrCreateUser(kakaoUserInfo);

            // then
            assertThat(foundUser.getKakaoId()).isEqualTo("1234567890");

            // 기존 회원이므로 save() 미호출 검증
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("[Ut-OAuth2UserSvc-properties누락시기본값적용-성공]")
        void properties누락시기본값적용_성공() {
            // given
            // properties 없이 id만 있는 최소 응답 구조(불완전한 응답)
            KakaoUserInfo minimalUserInfo = new KakaoUserInfo(Map.of("id", 9999L));

            given(userRepository.findByKakaoId("9999"))
                    .willReturn(Optional.empty());
            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            User foundUser = customOAuth2UserService.findOrCreateUser(minimalUserInfo);

            // then
            // KakaoUserInfo의 null 방어 로직 검증
            assertThat(foundUser.getNickname()).isEqualTo("닉네임");
            assertThat(foundUser.getProfileImageUrl()).isNull();
        }
    }
}