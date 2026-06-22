package com.mycpt.backend.domain.chemistry.dto;

import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;

import java.time.LocalDateTime;

// GET /chemistry-reports 목록 아이템
public record ChemistryReportSummary(
        Long reportId,
        Long requesterId,
        Long partnerId,
        String status,
        LocalDateTime createdAt
) {
    public static ChemistryReportSummary from(ChemistryReport cr) {
        return new ChemistryReportSummary(
                cr.getId(),
                cr.getRequester().getId(),
                cr.getPartner().getId(),
                cr.getStatus().name(),
                cr.getCreatedAt()
        );
    }
}
