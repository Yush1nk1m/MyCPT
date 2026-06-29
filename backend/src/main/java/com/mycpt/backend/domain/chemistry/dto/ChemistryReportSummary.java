package com.mycpt.backend.domain.chemistry.dto;

import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;

import java.time.LocalDateTime;

// GET /chemistry-reports 목록 아이템
public record ChemistryReportSummary(
        Long reportId,
        Long requesterId,
        String requesterNickname,
        Long partnerId,
        String partnerNickname,
        String status,
        LocalDateTime createdAt
) {
    public static ChemistryReportSummary from(ChemistryReport cr) {
        return new ChemistryReportSummary(
                cr.getId(),
                cr.getRequester().getId(),
                cr.getRequester().getNickname(),
                cr.getPartner().getId(),
                cr.getPartner().getNickname(),
                cr.getStatus().name(),
                cr.getCreatedAt()
        );
    }
}
