package com.mycpt.backend.domain.statistics.dto;

import java.util.List;

public record TrendResponse(
        Summary summary,
        List<TrendEntry> trend
) {

    public record Summary(
            String period,
            DiscAverageDto average,
            long count
    ) {}

    public record TrendEntry(
            DiscBucketsDto buckets,
            String createdAt
    ) {}
}
