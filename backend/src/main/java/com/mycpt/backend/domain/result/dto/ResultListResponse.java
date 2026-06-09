package com.mycpt.backend.domain.result.dto;

import java.util.List;

/**
 * GET /results 목록 응답 래퍼 DTO
 *
 * nextCursor: 다음 요청에 사용할 커서. 다음 페이지 없으면 null
 * hasNext: 다음 페이지 존재 여부
 */
public record ResultListResponse(
        List<ResultSummaryResponse> results,
        Long nextCursor,
        boolean hasNext
) {}
