package com.mycpt.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(securityComponents());
    }

    private Info apiInfo() {
        return new Info()
                .title("MyCPT API")
                .description("""
                        MyCPT REST API 명세서.
                        
                        ## 인증
                        카카오 OAuth2 세션 기반 인증을 사용한다.
                        1. `GET /api/v0/auth/kakao` 로 카카오 로그인을 진행한다.
                        2. 로그인 완료 후 발급된 JSESSIONID 쿠키가 이후 요청에 자동 포함된다.
                        3. 인증이 필요한 엔드포인트는 우측 자물쇠 아이콘으로 표시된다.
                        
                        ## 비회원 허용 엔드포인트
                        - `GET /api/v0/questions`
                        - `POST /api/v0/results/score`
                        - `GET /api/v0/assessments/{token}`
                        - `POST /api/v1/assessments/{token}/submit`
                        """)
                .version("v1")
                .contact(new Contact()
                        .name("Yushin Kim")
                        .email("kys010306@sogang.ac.kr"));
    }

    private Components securityComponents() {
        return new Components()
                // AuthApi.java의 @SecurityRequirement(name = "cookieAuth")와 이름이 일치해야 Swagger UI에서 자물쇠 아이콘 연동됨
                .addSecuritySchemes("cookieAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                // 인증 값을 쿠키에서 읽어옴. 세션 쿠키 방식임을 명시
                                .in(SecurityScheme.In.COOKIE)
                                // 브라우저가 전송하는 세션 쿠키 이름. SecurityConfig의 .deleteCookies("JSESSIONID")와 일치 필요
                                .name("accessToken")
                                .description("카카오 로그인 후 발급되는 JWT 액세스 토큰 쿠키"));
    }
}
