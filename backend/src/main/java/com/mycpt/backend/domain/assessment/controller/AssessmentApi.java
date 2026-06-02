package com.mycpt.backend.domain.assessment.controller;

import com.mycpt.backend.domain.assessment.dto.CreateTokenRequest;
import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.result.dto.ScoreRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "타인 평정", description = "타인 평정 링크 생성/접속/제출")
public interface AssessmentApi {

    @Operation(
            summary = "타인 평정 링크 생성",
            description = "일회용 평정 링크를 생성한다. 코인 소모 없음.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    ResponseEntity<Map<String, Object>> createToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateTokenRequest request
    );

    @Operation(
            summary = "평정 링크 접속",
            description = "토큰 유효성 확인 후 평정 대상자 정보를 반환한다. 비회원 가능."
    )
    ResponseEntity<Map<String, Object>> getSubjectInfo(
            @PathVariable String token
    );

    @Operation(
            summary = "타인 평정 제출",
            description = "채점 + tests/disc_results INSERT + used=TRUE 처리가 단일 트랜잭션으로 실행된다. 비회원 가능."
    )
    ResponseEntity<Map<String, String>> submit(
            @PathVariable String token,
            @RequestBody ScoreRequest request
    );
}
