package com.mycpt.backend.domain.auth.controller;

import com.mycpt.backend.support.MvcTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest: 웹 레이어(Controller + Security Filter Chain)만 로드
// Spring Security 필터 체인이 포함되므로 실제 인증/인가 동작 검증 가능
// JPA, Service 등 다른 레이어는 로드하지 않으므로 신속한 테스트 가능
@WebMvcTest(AuthV1Controller.class)
@DisplayName("AuthV1Controller 슬라이스 테스트")
class AuthV1ControllerTest extends MvcTestSupport {

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    class GetMe {

        @Test
        @DisplayName("[ST-AuthController-사용자인증-성공]")
        void 사용자인증_성공() throws Exception {
            // 필터를 실제 실행하여 인증 흐름 자체를 검증
            mockMvc.perform(get("/api/v1/auth/me")
                            // authenticated(): JwtAuthenticationFilter 실제 실행 경로를 시뮬레이션
                            .with(authenticated(testUser())))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[ST-AuthController-사용자인증-미인증접근]")
        void 사용자인증_미인증접근() throws Exception {
            // 쿠키 없이 요청 시 JwtAuthenticationFilter가 SecurityContext에 인증 객체를 주입하지 않음
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized())
                    // SecurityConfig의 authenticationEntryPoint가 가로채 401 + JSON 바디 반환
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.code", is("UNAUTHORIZED")))
                    .andExpect(jsonPath("$.message", is("인증이 필요합니다.")));
        }

        @Test
        @DisplayName("[ST-AuthController-응답바디형식확인-성공")
        void 응답바디형식확인_성공() throws Exception {
            // 응답 바디의 7개 필드 존재 여부 및 초기 값 검증
            mockMvc.perform(get("/api/v1/auth/me")
                            .with(authenticated(testUser())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    // testUser()는 DB 저장 전 객체라 null이므로 hasKey() 메서드로 키 존재 여부만 검증
                    .andExpect(jsonPath("$", hasKey("userId")))
                    .andExpect(jsonPath("$.nickname", is("테스트유저")))
                    .andExpect(jsonPath("$.profileImageUrl", is("https://k.kakaocdn.net/test.jpg")))
                    .andExpect(jsonPath("$.coins", is(3)))
                    // nextCoinAt은 신규 가입 시 만충(coins=3) 상태이므로 null
                    .andExpect(jsonPath("$.nextCoinAt", nullValue()))
                    // birthYear, gender는 로그인 후 프로필 변경 전까지 null
                    .andExpect(jsonPath("$.birthYear", nullValue()))
                    .andExpect(jsonPath("$.gender", nullValue()));

        }
    }
}