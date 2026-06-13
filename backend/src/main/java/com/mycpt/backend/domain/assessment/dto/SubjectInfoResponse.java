package com.mycpt.backend.domain.assessment.dto;

import com.mycpt.backend.domain.assessment.service.AssessmentService;

/**
 * GET /assessments/{token} 응답 DTO
 */
public record SubjectInfoResponse(String subjectNickname, String subjectProfileImageUrl) {
    public static SubjectInfoResponse from(AssessmentService.SubjectInfo info) {
        return new SubjectInfoResponse(info.subjectNickname(), info.subjectProfileImageUrl());
    }
}
