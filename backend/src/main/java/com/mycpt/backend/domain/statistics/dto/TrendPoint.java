package com.mycpt.backend.domain.statistics.dto;

import java.time.LocalDateTime;

public record TrendPoint(
        int dBucket,
        int iBucket,
        int sBucket,
        int cBucket,
        LocalDateTime createdAt
) {}
