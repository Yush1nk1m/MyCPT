package com.mycpt.backend.domain.coin.dto;

import java.util.List;

// GET /coins/history 응답
public record CoinHistoryResponse(
        List<CoinTransactionResponse> history,
        Long nextCursor,
        boolean hasNext
) {
}
