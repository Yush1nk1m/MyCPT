package com.mycpt.backend.domain.auth.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

// Swagger 문서화 및 API 계약을 담당하는 인터페이스
// 구현 로직은 AuthController에서 담당하고 이 인터페이스는 "무엇을 제공하는가"만 정의
//
// 장점:
// - Swagger 애너테이션이 구현체에 섞이지 않아 컨트롤러 코드의 가독성 향상
// - 버전 변경 시 AuthV0Controller implements AuthApi 구문으로 스펙 재사용 가능
// - 스펙 변경 시 인터페이스만 수정하면 모든 버전 구현체에서 컴파일 오류로 즉시 감지 가능
@Tag(
        name = "Auth",
        description = "카카오 OAuth2 로그인/로그아웃 및 현재 사용자 조회 API"
)
public interface AuthApi {

    // GET /api/v0/auth/kakao
    @Operation(
            summary = "카카오 로그인",
            description = "카카오 OAuth2 인증 페이지로 리다이렉트한다. " +
                    "returnTo 파라미터가 있으면 로그인 완료 후 해당 경로로 이동한다. " +
                    "허용 경로: /, /save-result. 그 외는 /로 이동."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "카카오 인증 URL로 리다이렉트"
            )
    })
    void kakaoLogin(
            @Parameter(description = "로그인 완료 후 이동할 경로. 내부 경로만 허용.")
            @RequestParam(required = false) String returnTo,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException;

    // GET /api/v1/auth/me
    @Operation(
            summary = "내 정보 조회",
            description = "현재 세션의 로그인 사용자 정보를 반환한다. " +
                    "페이지 로드 시 로그인 상태 확인 및 헤더 렌더링에 활용한다.",
            // Swagger UI에서 이 엔드포인트 호출 시 인증(세션 쿠키)이 필요함을 명시
            // SwaggerConfig에서 정의한 보안 스킴 이름과 일치해야 함
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "userId": 1,
                                        "nickname": "유신",
                                        "profileImageUrl": "https://k.kakaocdn.net/...",
                                        "coins": 2,
                                        "nextCoinAt": "2026-05-30T14:00:00",
                                        "birthYear": null,
                                        "gender": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "UNAUTHORIZED",
                                        "message": "인증이 필요합니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal UserPrincipal principal);
}
