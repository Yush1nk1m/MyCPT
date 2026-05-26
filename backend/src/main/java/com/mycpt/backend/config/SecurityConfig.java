package com.mycpt.backend.config;

import com.mycpt.backend.domain.auth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity  // 보안 설정을 위한 Spring Security 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    // 카카오 OAuth2 로그인 완료 후 사용자 정보를 처리하기 위한 서비스
    // loadUser() 메서드를 사용해 신규 회원 가입 또는 기존 회원 조회 수행
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. CSRF 토큰을 비활성화. CORS 정책을 통해 동일한 보안 효과 제공 가능
        http.csrf(csrf -> csrf.disable());

        // 2. CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 3. 엔드포인트별 인증 규칙 설정
        http.authorizeHttpRequests(auth -> auth
                // 3-1. Swagger UI 접근 허용. 인증 없이 API 문서 확인 및 수동 테스트 가능
                .requestMatchers(
                        "/swagger-ui/**",   // 3-5-1. Swagger UI 정적 리소스
                        "/v3/api-docs/**"   // 3-5-2. OpenAPI 스펙 JSON 엔드포인트
                ).permitAll()
                // 3-2. 비회원도 검사 문항 조회 가능(GET /api/v1/questions)
                .requestMatchers(HttpMethod.GET, "/api/v1/questions").permitAll()
                // 3-3. 비회원도 채점 요청 가능 (POST /api/v1/results/score)
                .requestMatchers(HttpMethod.POST, "/api/v1/results/score").permitAll()
                // 3-4. 비회원도 타인 평정 링크 조회 가능 (GET /api/v1/assessments/{token})
                .requestMatchers(HttpMethod.GET, "/api/v1/assessments/*").permitAll()
                // 3-5. 비회원도 타인 평정 결과 제출 가능
                .requestMatchers(HttpMethod.POST, "/api/v1/assessments/*/submit").permitAll()
                // 3-6. 위 규칙에 해당하지 않는 모든 요청은 로그인 필요. 미인증 시 authenticationEntryPoint에서 401 상태 반환
                .anyRequest().authenticated());

        // 4. OAuth2 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
                // 4-1. 카카오에서 사용자 정보를 받아온 뒤 호출할 서비스 지정
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService))
                // 4-2. 로그인 성공 시 리다이렉트할 URL. true = 이전에 접근하려던 URL이 있더라도 무조건 이 URL로 이동
                .defaultSuccessUrl("http://localhost:3000", true)
                // 4-3. 로그인 실패 시 리다이렉트할 URL
                .failureUrl("http://localhost:3000/login?error"));

        // 5. 로그아웃 설정
        http.logout(logout -> logout
                // 5-1. 기본 로그아웃 URL은 /logout이지만 API 명세에 맞게 재정의. POST /api/v1/auth/logout 요청 시 Spring Security가 자동 처리
                .logoutUrl("/api/v1/auth/logout")
                // 5-2. 로그아웃 성공 핸들러. REST API 클라이언트는 리다이렉트가 아닌 200 응답 필요
                .logoutSuccessHandler((request, response, authentication) ->
                        response.setStatus(HttpServletResponse.SC_OK))
                // 5-3. 서버 측 세션 데이터 삭제
                .invalidateHttpSession(true)
                // 5-4. 브라우저의 JSESSIONID 쿠키 만료 처리
                .deleteCookies("JSESSIONID"));

        // 6. 미인증 요청 처리
        http.exceptionHandling(ex -> ex
                // 6-1. 인증이 필요한 엔드포인트에 미인증 상태로 접근 시 호출. 기본 동작은 302이지만 REST API이므로 401 응답 필요
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}");
                }));

        return http.build();
    }

    // CORS 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 출처 설정
        // TODO: 운영환경 배포 시 실제 도메인으로 교체
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // 허용할 HTTP 메서드 설정. OPTIONS는 브라우저 preflight 요청에 필요
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 요청 헤더
        config.setAllowedHeaders(List.of("*"));

        // 쿠키(JSESSIONID)를 포함한 요청 허용. true여야 세션 기반 인증이 동작. allowedOrigins가 와일드카드일 시 설정 불가
        config.setAllowCredentials(true);

        // /** 패턴으로 모든 경로에 위 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
