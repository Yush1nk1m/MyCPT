package com.mycpt.backend.domain.auth.dto;

import com.mycpt.backend.domain.user.entity.User;

import java.time.LocalDateTime;

/**
 * GET /auth/me 응답 DTO
 * gender: nullable (프로필 미입력), nextCoinAt: nullable (만충)
 */
public record MeResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        int coins,
        LocalDateTime nextCoinAt,
        Integer birthYear,
        String gender
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getCoins(),
                user.getNextCoinAt(),
                user.getBirthYear(),
                user.getGender() != null ? user.getGender().name() : null
        );
    }
}
