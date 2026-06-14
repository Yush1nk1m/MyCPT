package com.mycpt.backend.domain.statistics.dto;

public record BucketAverage(
        double dBucket,
        double iBucket,
        double sBucket,
        double cBucket,
        long sampleCount
) {}
