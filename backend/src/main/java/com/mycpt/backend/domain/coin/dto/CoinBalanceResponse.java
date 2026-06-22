package com.mycpt.backend.domain.coin.dto;

import java.time.LocalDateTime;

// GET /coins 응
public record CoinBalanceResponse(
        int coins,
        LocalDateTime nextCoinAt
) {
}
