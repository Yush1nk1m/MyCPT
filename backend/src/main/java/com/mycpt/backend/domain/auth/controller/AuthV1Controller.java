package com.mycpt.backend.domain.auth.controller;

import com.mycpt.backend.config.JwtProvider;
import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// AuthApi 인터페이스의 V1 구현체
// Swagger 문서와 관련한 애너테이션은 AuthApi 인터페이스에 집중
// 구현 클래스는 "어떻게 구현하는가"에 관한 순수한 구현 로직만 담당
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthV1Controller implements AuthApi {

    private final JwtProvider jwtProvider;

    // GET /api/v1/auth/kakao
    @GetMapping("/kakao")
    @Override
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        // Spring Security OAuth2 진입점으로 위임
        // 리다이렉트 이후의 모든 흐름(카카오 인증 페이지 -> 콜백 -> 세션 발급)은 Spring Security, CustomOAuth2UserService가 자동 처리
        response.sendRedirect("/oauth2/authorization/kakao");
    }

    // GET /api/v1/auth/me
    @GetMapping("/me")
    @Override
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal UserPrincipal principal) {

        User user = principal.getUser();

        // coins < 3 일 때 프론트엔드가 다음 충전까지 남은 시간 UI를 렌더링할 수 있도록 nextCoin을 응답에 포함
        // coins == 3 (만충) 일 때 nextCoinAt은 null로 반환
        // LocalDateTime -> JSON 직렬화 시 "2026-05-30T14:00:00" 형식으로 반환 필요
        // -> application.yml에 Jackson 날짜 포맷 설정 필요

        // LinkedHashMap: 삽입 순서를 보장하여 응답 JSON 필드 순서를 API 스펙과 일치
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", user.getId());
        body.put("nickname", user.getNickname());
        body.put("profileImageUrl", user.getProfileImageUrl());
        body.put("coins", user.getCoins());
        body.put("nextCoinAt", user.getNextCoinAt());
        body.put("birthYear", user.getBirthYear());
        body.put("gender", user.getGender() != null ? user.getGender().name() : null);

        return ResponseEntity.ok(body);
    }

    // POST /api/v1/auth/logout
    // SecurityConfig 클래스에서 logoutUrl() 메서드로 설정했으므로 Spring Security 필터가 POST 요청을 가로채 직접 처리
    // 처리 순서: 세션 무효화 -> JSESSIONID 쿠키 만료 -> 200 응답
    // 필터에서 처리 후 응답하기 때문에 Filter -> DispatcherServlet -> Controller 방향으로 진행 안 함
}
