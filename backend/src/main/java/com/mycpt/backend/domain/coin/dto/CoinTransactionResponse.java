package com.mycpt.backend.domain.coin.dto;

import com.mycpt.backend.domain.coin.entity.CoinTransaction;

import java.time.LocalDateTime;

// GET /coins/history의 history 배열 항
public record CoinTransactionResponse(
        Long transactionId,
        int amount,
        String reason,
        int balanceAfter,
        LocalDateTime createdAt
) {

    public static CoinTransactionResponse from(CoinTransaction tx) {
        return new CoinTransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getReason().name(),
                tx.getBalanceAfter(),
                tx.getCreatedAt()
        );
    }
}
