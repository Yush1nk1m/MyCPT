package com.mycpt.backend.domain.assessment.dto;

import com.mycpt.backend.domain.assessment.service.AssessmentService;

import java.time.LocalDateTime;

/**
 * POST /assessments 응답 DTO
 */
public record CreateTokenResponse(String token, LocalDateTime expiresAt) {
    public static CreateTokenResponse from(AssessmentService.TokenInfo result) {
        return new CreateTokenResponse(result.token(), result.expiresAt());
    }
}
