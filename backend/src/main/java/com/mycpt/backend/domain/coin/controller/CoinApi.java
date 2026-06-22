package com.mycpt.backend.domain.coin.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.coin.dto.CoinBalanceResponse;
import com.mycpt.backend.domain.coin.dto.CoinHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "코인", description = "코인 잔액 조회/이력")
public interface CoinApi {

    @Operation(summary = "코인 잔액 조회")
    ResponseEntity<CoinBalanceResponse> getBalance(
            @AuthenticationPrincipal UserPrincipal principal
    );

    @Operation(summary = "코인 사용/충전 이력 조회")
    ResponseEntity<CoinHistoryResponse> getHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    );
}
