package com.mycpt.backend.domain.result.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.result.dto.*;
import com.mycpt.backend.domain.result.enums.RaterType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Result 도메인 API 계약 인터페이스
 *
 * Swagger 문서화 애너테이션을 구현체(ResultV1Controller)와 분리하여 컨트롤러 코드가 순수 구현 로직에만 집중하도록 함
 * AuthApi와 동일한 Interface + V1Controller 컨벤션 준수
 */
@Tag(name = "Result", description = "DISC 채점 및 결과 저장/조회 API")
public interface ResultApi {

    @Operation(
            summary = "채점 및 결과 반환",
            description = "프론트엔드에서 산출한 DISC 원점수를 전달받아 버킷 값을 산출하고 분석 보고서를 반환한다. " +
                    "비회원은 응답의 scores 객체를 sessionStorage에 보관 후 로그인 시 POST /results 로 전송한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "testType": "DISC",
                                        "scores": { "d": 32, "i": 10, "s": -4, "c": -14 },
                                        "buckets": { "d": 8, "i": 5, "s": 3, "c": 2 },
                                        "report": "## 결과 개요\\\\n..."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "원점수 범위 오류 / D+I+S+C 합계가 24가 아님",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "INVALID_SCORE",
                                        "message": "D+I+S+C 합계는 24여야 합니다. 입력값: 25"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ScoreResponse> score(@RequestBody ScoreRequest request);

    @Operation(
            summary = "결과 저장",
            description = "sessionStorage에 보관하던 DISC 원점수를 서버에 저장한다. 회원 전용.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "resultId": 42 }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "원점수 범위 오류 / 합계 불일치"),
            @ApiResponse(responseCode = "401", description = "미인증")
    })
    ResponseEntity<SaveResponse> save(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ScoreRequest request
    );

    @Operation(
            summary = "결과 이력 목록 조회",
            description = "로그인한 회원의 검사 결과 목록을 최신순으로 반환한다. 커서 기반 페이지네이션.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "미인증")
    })
    ResponseEntity<ResultListResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) RaterType raterType,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int size
    );

    @Operation(
            summary = "결과 상세 조회",
            description = "특정 검사 결과의 상세 정보와 분석 보고서를 반환한다. 본인 결과만 조회 가능.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "미인증"),
            @ApiResponse(responseCode = "403", description = "본인 결과가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 결과 ID")
    })
    ResponseEntity<ResultDetailResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    );
}
