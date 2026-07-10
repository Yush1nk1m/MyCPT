package com.mycpt.backend.domain.user.dto;

public record WithdrawRequest(
        String reason   // optional, 최대 200자
) {}
