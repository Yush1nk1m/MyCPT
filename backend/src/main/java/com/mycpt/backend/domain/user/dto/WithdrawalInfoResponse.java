package com.mycpt.backend.domain.user.dto;

public record WithdrawalInfoResponse(
        long resultCount,
        long chemistryCount,
        long colleagueCount
) {}
