package com.mycpt.backend.domain.auth.controller;

import com.mycpt.backend.config.SecurityConfig;
import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.auth.service.CustomOAuth2UserService;
import com.mycpt.backend.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest: 웹 레이어(Controller + Filter)만 로드
// Spring Security 필터 체인이 포함되므로 실제 인증/인가 동작 검증 가능
// JPA, Service 등 다른 레이어는 로드하지 않으므로 신속한 테스트 가능
@WebMvcTest(AuthV1Controller.class)
@Import(SecurityConfig.class)
@DisplayName("AuthV1Controller 슬라이스 테스트")
class AuthV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @WebMvcTest는 Spring Security 자동 설정 포함
    // SecurityConfig가 CustomOAuth2UserService를 의존하므로 MockitoBean으로 등록하여 컨텍스트 로드 오류 방지
    // 실제 카카오 API 호출 없이 Security 설정만 적용
    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    // oauth2Login() 메서드에 주입해 인증된 사용자를 시뮬레이션하기 위한 User 엔티티와 UserPrincipal
    private User testUser;
    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        // 리플렉션으로 id 필드를 설정할 수 없으므로 id가 null인 상태로 테스트
        // id 필드 검증은 AUC-03에서 null이 아닌 키 존재 여부로 검증
        testUser = User.create(
                "1234567890",
                "테스트유저",
                "https://k.kakaocdn.net/test.jpg");

        testPrincipal = new UserPrincipal(testUser, Map.of(
                "id", 1234567890L,
                "properties", Map.of(
                        "nickname", "테스트유저",
                        "profile_image", "https://k.kakaocdn.net/test.jpg")));
    }

    @Nested
    @DisplayName("GET /api/v1/auth/me - AUC")
    class GetMe {

        @Test
        @DisplayName("[AUC-01] 인증된 사용자 /auth/me 200 응답")
        void AUC_01() throws Exception {
            // oauth2Login(): Spring Security Test가 제공하는 OAuth2 인증 주입
            // @WithMockUser 사용 시 단순 문자열 기반이라 UserPrincipal 타입 주입 불가능
            // oauth2Login().oauth2User(testPrincipal) 메서드 체인으로 UserPrincipal을 SecurityContext에 주입
            mockMvc.perform(get("/api/v1/auth/me")
                            .with(oauth2Login().oauth2User(testPrincipal)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[AUC-02] 미인증 사용자 /auth/me 401 응답")
        void AUC_02() throws Exception {
            // 인증 정보 없이 호출
            // SecurityConfig의 authenticationEntryPoint가 가로채 401 + JSON 응답 바디 반환
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is("UNAUTHORIZED")))
                    .andExpect(jsonPath("$.message", is("인증이 필요합니다.")));
        }

        @Test
        @DisplayName("[AUC-03] /auth/me 응답 바디 전체 필드 검증")
        void AUC_03() throws Exception {
            // 7개 필드가 모두 응답 바디에 존재하는지 검증
            // hasKey(): 키 존재 여부만 검증. 값 타입은 별도 검증
            mockMvc.perform(get("/api/v1/auth/me")
                            .with(oauth2Login().oauth2User(testPrincipal)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$", hasKey("userId")))
                    .andExpect(jsonPath("$.nickname", is("테스트유저")))
                    .andExpect(jsonPath("$.profileImageUrl", is("https://k.kakaocdn.net/test.jpg")))
                    .andExpect(jsonPath("$.coins", is(3)))
                    .andExpect(jsonPath("$.nextCoinAt", nullValue()))
                    .andExpect(jsonPath("$.birthYear", nullValue()))
                    .andExpect(jsonPath("$.gender", nullValue()));
        }
    }
}