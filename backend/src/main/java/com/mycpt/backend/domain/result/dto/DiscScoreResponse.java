package com.mycpt.backend.domain.result.dto;

/**
 * POST /results/score 응답 DTO
 *
 * 비회원은 scores를 sessionStorage에 보관 후 POST /results 로 재전송
 * buckets: MinDiscBars / DiscBarsLarge 렌더링 용도 (1~3)
 */
public record ScoreResponse(
        DiscScores scores,
        DiscBuckets buckets,
        String report
) {}
