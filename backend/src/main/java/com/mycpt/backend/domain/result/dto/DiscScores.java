package com.mycpt.backend.domain.result.dto;

/**
 * DISC 원점수 공용 DTO (-24~+48)
 * 결과 상세 등 원점수가 필요한 응답에서 재사용
 */
public record DiscScores(int d, int i, int s, int c) {}
