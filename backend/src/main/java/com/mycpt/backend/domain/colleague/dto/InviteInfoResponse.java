package com.mycpt.backend.domain.colleague.dto;

// 초대 링크 진입 시 초대자 정보 반환
public record InviteInfoResponse(
        Long inviterId,
        String nickname,
        String profileImageUrl
) {}
