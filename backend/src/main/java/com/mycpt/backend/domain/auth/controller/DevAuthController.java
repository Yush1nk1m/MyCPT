package com.mycpt.backend.domain.auth.controller;

import com.mycpt.backend.config.JwtProvider;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 개발 환경 전용 — 카카오 로그인 없이 즉시 로그인 처리
 *
 * @Profile("!prod") — 운영 환경에서는 이 빈 자체가 로드되지 않아
 * SecurityConfig permitAll 설정과 무관하게 라우트가 존재하지 않음 (404)
 *
 * 사용법: GET /api/v1/dev/login?kakaoId=dev-a&nickname=테스터A&returnTo=/invite/AB3D9K2M
 * kakaoId로 기존 유저 조회, 없으면 즉시 생성 후 accessToken 쿠키 발급 + 프론트로 리다이렉트
 * 기존 Kakao successHandler(SecurityConfig)와 동일한 쿠키 발급 로직 재사용
 */
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
@Profile("!prod")
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @GetMapping("/login")
    public void devLogin(
            @RequestParam String kakaoId,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String returnTo,
            HttpServletResponse response
    ) throws IOException {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(
                        User.create(kakaoId, nickname != null ? nickname : kakaoId, null)
                ));

        String token = jwtProvider.generateToken(user.getId());

        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProvider.getExpirationMs() / 1000));
        response.addCookie(cookie);

        String target = isSafeRedirect(returnTo) ? returnTo : "/";
        response.sendRedirect("http://localhost:3000" + target);
    }

    private static boolean isSafeRedirect(String path) {
        if (path == null || path.isBlank()) return false;
        if (path.startsWith("http") || path.startsWith("//")) return false;
        return path.startsWith("/");
    }
}
