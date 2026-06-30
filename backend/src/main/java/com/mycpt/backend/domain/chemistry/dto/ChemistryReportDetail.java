package com.mycpt.backend.domain.chemistry.dto;

import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;

import java.time.LocalDateTime;

// GET /chemistry-reports/{id} 응답
public record ChemistryReportDetail(
        Long reportId,
        UserInfo requester,
        UserInfo partner,
        String myRole,  // "REQUESTER" | "PARTNER"
        String status,
        String report,
        LocalDateTime createdAt
) {
    public record UserInfo(Long userId, String nickname, String profileImageUrl) {}

    public static ChemistryReportDetail from(ChemistryReport cr, Long userId) {
        String myRole = cr.getRequester().getId().equals(userId) ? "REQUESTER" : "PARTNER";

        // 조회 시점 이름 치환 - chemistry_cache에는 플레이스홀더 원문 저장
        // 캐시는 닉네임과 무관하게 공유되는 원문, 개인화는 조회 시점 발생
        String report = null;
        if (cr.getChemistryCache() != null && cr.getChemistryCache().getReport() != null) {
            report = cr.getChemistryCache().getReport()
                    .replace("{REQUESTER}", cr.getRequester().getNickname() + "님")
                    .replace("{PARTNER}", cr.getPartner().getNickname() + "님");
        }

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
                myRole,
                cr.getStatus().name(),
                report,
                cr.getCreatedAt()
        );
    }
}
