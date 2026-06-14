package com.mycpt.backend.domain.statistics.dto;

public record ComparisonResponse(
        My my,
        Average average
) {

    public record My(DiscBucketsDto buckets) {}

    public record Average(
            String ageGroup,
            String gender,
            DiscAverageDto buckets,
            long sampleCount
    ) {}
}
