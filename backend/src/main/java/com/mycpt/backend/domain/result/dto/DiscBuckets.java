package com.mycpt.backend.domain.result.dto;

/**
 * DISC 버킷 값 공용 DTO (1~3)
 * 결과 목록, 결과 상세, 통계, 케미 등 버킷 값이 필요한 모든 응답에서 재사용
 */
public record DiscBuckets(int d, int i, int s, int c) {}
