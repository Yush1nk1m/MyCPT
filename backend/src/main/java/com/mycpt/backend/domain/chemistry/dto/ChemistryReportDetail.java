package com.mycpt.backend.domain.chemistry.dto;

import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;

import java.time.LocalDateTime;

// GET /chemistry-reports/{id} 응답
public record ChemistryReportDetail(
        Long reportId,
        UserInfo requester,
        UserInfo partner,
        String status,
        String report,
        LocalDateTime createdAt
) {
    public record UserInfo(Long userId, String nickname, String profileImageUrl) {}

    public static ChemistryReportDetail from(ChemistryReport cr) {
        return new ChemistryReportDetail(
                cr.getId(),
                new UserInfo(
                        cr.getRequester().getId(),
                        cr.getRequester().getNickname(),
                        cr.getRequester().getProfileImageUrl()
                ),
                new UserInfo(
                        cr.getPartner().getId(),
                        cr.getPartner().getNickname(),
                        cr.getPartner().getProfileImageUrl()
                ),
                cr.getStatus().name(),
                cr.getChemistryCache().getReport(),
                cr.getCreatedAt()
        );
    }
}
