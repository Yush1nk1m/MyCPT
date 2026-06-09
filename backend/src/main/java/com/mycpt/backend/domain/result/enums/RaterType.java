package com.mycpt.backend.domain.result.enums;

/**
 * 검사 평정 유형
 *
 * SELF:    자기 평정 (POST /results - 회원)
 * OTHER:   타인 평정 (POST /assessments/{token}/submit - 비회원 가능)
 */
public enum RaterType {
    SELF,
    OTHER
}
