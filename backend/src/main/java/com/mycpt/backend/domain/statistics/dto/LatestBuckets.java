package com.mycpt.backend.domain.statistics.dto;

public record LatestBuckets(
        int dBucket,
        int iBucket,
        int sBucket,
        int cBucket
) {}
