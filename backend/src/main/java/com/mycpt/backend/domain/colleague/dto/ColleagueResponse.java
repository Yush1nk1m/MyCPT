package com.mycpt.backend.domain.colleague.dto;

import com.mycpt.backend.domain.colleague.entity.Colleague;

import java.time.LocalDateTime;

// POST /colleagues 201, GET /colleagues 아이템, GET /colleagues/{partnerId} 응답 공용
public record ColleagueResponse(
        Long partnerId,
        String nickname,
        String profileImageUrl,
        LocalDateTime connectedAt
) {
    // myUserId 기준으로 상대방 정보를 추출
    public static ColleagueResponse from(Colleague colleague, Long myUserId) {
        var partner = colleague.getPartner(myUserId);
        return new ColleagueResponse(
                partner.getId(),
                partner.getNickname(),
                partner.getProfileImageUrl(),
                colleague.getCreatedAt()
        );
    }
}
