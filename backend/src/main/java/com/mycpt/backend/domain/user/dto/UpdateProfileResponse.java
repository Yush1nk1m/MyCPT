package com.mycpt.backend.domain.user.dto;

import com.mycpt.backend.domain.user.entity.User;

/**
 * PATCH /users/me 응답 DTO
 */
public record UpdateProfileResponse(
        String nickname,
        Integer birthYear,
        String gender
) {
    public static UpdateProfileResponse from(User user) {
        return new UpdateProfileResponse(
                user.getNickname(),
                user.getBirthYear(),
                user.getGender() != null ? user.getGender().name() : null
        );
    }
}
