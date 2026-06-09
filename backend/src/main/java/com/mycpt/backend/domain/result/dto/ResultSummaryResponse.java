package com.mycpt.backend.domain.result.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mycpt.backend.domain.result.enums.RaterType;

import java.time.LocalDateTime;

/**
 * GET /results 목록 응답 DTO - 카드 1개에 필요한 최소 정보
 *
 * label: raterType=OTHER일 때만 값 있음. SELF면 null
 * buckets: 프론트 MiniDiscBars 렌더링 용도 (1~3)
 */
public record ResultSummaryResponse(
        Long resultId,
        RaterType raterType,
        String label,
        DiscBuckets buckets,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
