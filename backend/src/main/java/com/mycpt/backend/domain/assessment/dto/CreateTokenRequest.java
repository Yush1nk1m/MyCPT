package com.mycpt.backend.domain.assessment.dto;

/**
 * POST /assessments 요청 바디
 */
public record CreateTokenRequest(
        String label     // 평정자 식별 라벨 (optional, 최대 30자)
) {}
