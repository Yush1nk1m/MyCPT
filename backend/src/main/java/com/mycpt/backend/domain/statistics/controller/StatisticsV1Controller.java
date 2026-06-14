package com.mycpt.backend.domain.statistics.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.statistics.dto.ComparisonResponse;
import com.mycpt.backend.domain.statistics.dto.TrendResponse;
import com.mycpt.backend.domain.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsV1Controller implements StatisticsApi {

    private final StatisticsService statisticsService;

    @GetMapping("/comparison")
    @Override
    public ResponseEntity<ComparisonResponse> comparison(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                statisticsService.comparison(principal.getUser().getId())
        );
    }

    @GetMapping("/trend")
    @Override
    public ResponseEntity<TrendResponse> trend(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "DISC") String type,
            @RequestParam(defaultValue = "30") int days
    ) {
        // type 파라미터: MVP에서는 DISC만 지원. 추후 확장 시 StatisticsService에 전달
        return ResponseEntity.ok(
                statisticsService.trend(principal.getUser().getId(), days)
        );
    }
}
