package com.mycpt.backend.support;

import com.mycpt.backend.config.JwtProvider;
import com.mycpt.backend.config.SecurityConfig;
import com.mycpt.backend.domain.auth.service.CustomOAuth2UserService;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class MvcTestSupport {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtProvider jwtProvider;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected CustomOAuth2UserService customOAuth2UserService;

    // JwtAuthenticationFilter 실제 실행 경로:
    // 쿠키 추출 -> validate() -> getUserId() -> findById() -> SecurityContext 주입
    protected RequestPostProcessor authenticated(User user) {
        given(jwtProvider.validate(anyString())).willReturn(true);
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        return request -> {
            request.setCookies(new Cookie("accessToken", "test-token"));
            return request;
        };
    }

    protected User testUser() {
        return User.create(
                "1234567890",
                "테스트유저",
                "https://k.kakaocdn.net/test.jpg");
    }
}
