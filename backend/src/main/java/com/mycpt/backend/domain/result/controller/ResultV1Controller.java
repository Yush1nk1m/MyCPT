package com.mycpt.backend.domain.result.controller;

import com.mycpt.backend.domain.result.dto.ScoreRequest;
import com.mycpt.backend.domain.result.service.CacheService;
import com.mycpt.backend.domain.result.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ResultApi 인터페이스의 V1 구현체.
 *
 * 현재 구현 범위(2주차 Day 1):
 *   - POST /results/score: 원점수 검증 + 버킷 정규화 → 응답 반환
 *   - report 필드는 CacheService + LlmService 연동(Day 2~3) 전까지 null
 *
 * TODO:
 *   - POST /results: 회원 결과 저장 (ResultService, 2주차 Day 2~3)
 *   - GET /results: 결과 이력 조회 (ResultService, 3주차)
 *   - GET /results/{id}: 결과 상세 조회 (ResultService, 3주차)
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResultV1Controller implements ResultApi {

    private final ScoringService scoringService;
    private final CacheService cacheService;

    // POST /api/v1/results/score
    // 비회원 접근 가능 - SecurityConfig에서 접근 경로 permitAll() 메서드 적용
    @PostMapping("/results/score")
    @Override
    public ResponseEntity<Map<String, Object>> score(@RequestBody ScoreRequest request) {
        // 검증 + 버킷 정규화
        ScoringService.Buckets buckets = scoringService.normalize(request);
        ScoreRequest.Scores s = request.scores();

        // CacheService가 HIT/MISS/만료를 판단하고 보고서를 반환
        String report = cacheService.getReport(buckets);

        // scores: 요청받은 원점수를 그대로 응답에 포함 -> 비회원이 sessionStorage에 보관 후 POST /results 로 재전송 가능
        Map<String, Object> scores = Map.of(
                "d", s.d(), "i", s.i(), "s", s.s(), "c", s.c()
        );

        // buckets: 정규화된 버킷 값 (1~9). CacheService.getReport() 메서드 호출 키로 사용
        Map<String, Object> bucketMap = Map.of(
                "d", buckets.d(), "i", buckets.i(), "s", buckets.s(), "c", buckets.c()
        );

        // LinkedHashMap: scores -> buckets -> report 순서 고정
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scores", scores);
        body.put("buckets", bucketMap);
        body.put("report", report);

        return ResponseEntity.ok(body);
    }
}
