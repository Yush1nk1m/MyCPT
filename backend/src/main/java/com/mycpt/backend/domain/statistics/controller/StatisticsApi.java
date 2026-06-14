package com.mycpt.backend.domain.statistics.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.statistics.dto.ComparisonResponse;
import com.mycpt.backend.domain.statistics.dto.TrendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "통계", description = "통계 비교 및 변화 추이")
public interface StatisticsApi {

    @Operation(summary = "나이대/성별 평균 비교")
    ResponseEntity<ComparisonResponse> comparison(
            @AuthenticationPrincipal UserPrincipal principal
    );

    @Operation(summary = "변화 추이 조회")
    ResponseEntity<TrendResponse> trend(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "DISC") String type,
            @RequestParam(defaultValue = "30") int days
    );
}
