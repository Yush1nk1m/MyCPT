package com.mycpt.backend.domain.result.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.result.dto.*;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.result.service.CacheService;
import com.mycpt.backend.domain.result.service.ResultService;
import com.mycpt.backend.domain.result.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * ResultApi 인터페이스의 V1 구현체.
 *
 * 현재 구현 범위(2주차 Day 1):
 *   - POST /results/score: 원점수 검증 + 버킷 정규화 → 응답 반환
 *   - report 필드는 CacheService + LlmService 연동(Day 2~3) 전까지 null
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResultV1Controller implements ResultApi {

    private final ScoringService scoringService;
    private final CacheService cacheService;
    private final ResultService resultService;

    // POST /api/v1/results/score
    // 비회원 접근 가능 - SecurityConfig에서 접근 경로 permitAll() 메서드 적용
    @PostMapping("/results/score")
    @Override
    public ResponseEntity<ScoreResponse> score(@RequestBody ScoreRequest request) {
        // 검증 + 버킷 정규화
        ScoringService.Buckets buckets = scoringService.normalize(request);
        ScoreRequest.Scores s = request.scores();

        // CacheService가 HIT/MISS/만료를 판단하고 보고서를 반환
        String report = cacheService.getReport(buckets);

        return ResponseEntity.ok(new ScoreResponse(
                request.testType(),
                new DiscScores(s.d(), s.i(), s.s(), s.c()),
                new DiscBuckets(buckets.d(), buckets.i(), buckets.s(), buckets.c()),
                report
        ));
    }

    // POST /api/v1/results - 회원 전용
    @PostMapping("/results")
    @Override
    public ResponseEntity<SaveResponse> save(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ScoreRequest request
    ) {
        Long resultId = resultService.save(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SaveResponse(resultId));
    }

    // GET /api/v1/results - 회원 전용
    @GetMapping("/results")
    @Override
    public ResponseEntity<ResultListResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) RaterType raterType,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(resultService.list(principal.getUser().getId(), raterType, cursor, size));
    }

    // GET /api/v1/results/{id} - 회원 전용
    @GetMapping("/results/{id}")
    @Override
    public ResponseEntity<ResultDetailResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(resultService.detail(principal.getUser().getId(), id));
    }
}
