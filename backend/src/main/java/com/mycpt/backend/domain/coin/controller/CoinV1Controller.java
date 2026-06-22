package com.mycpt.backend.domain.coin.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.coin.dto.CoinBalanceResponse;
import com.mycpt.backend.domain.coin.dto.CoinHistoryResponse;
import com.mycpt.backend.domain.coin.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coins")
@RequiredArgsConstructor
public class CoinV1Controller implements CoinApi {

    private final CoinService coinService;

    @GetMapping
    @Override
    public ResponseEntity<CoinBalanceResponse> getBalance(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                coinService.getBalance(principal.getUser().getId())
        );
    }

    @GetMapping("/history")
    @Override
    public ResponseEntity<CoinHistoryResponse> getHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                coinService.getHistory(principal.getUser().getId(), cursor, size)
        );
    }
}
