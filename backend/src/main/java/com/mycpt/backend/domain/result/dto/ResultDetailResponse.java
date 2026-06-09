package com.mycpt.backend.domain.result.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mycpt.backend.domain.result.enums.RaterType;

import java.time.LocalDateTime;

/**
 * GET /results/{id} 상세 응답 DTO
 *
 * scores: 원점수 (-24~+48). 상세 화면 표시 용도
 * buckets: 버킷 값 (1~3). DiscBarsLarge 렌더링 용도
 * report: Markdown 전문. react-markdown으로 렌더링
 */
public record ResultDetailResponse(
        Long resultId,
        RaterType raterType,
        String label,
        DiscScores scores,
        DiscBuckets buckets,
        String report,
        @JsonFormat(pattern = "yyyy-MM-DD'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
