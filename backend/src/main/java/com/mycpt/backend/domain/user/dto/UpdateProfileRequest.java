package com.mycpt.backend.domain.user.dto;

import com.mycpt.backend.domain.user.enums.Gender;

/**
 * PATCH /users/me 요청 바디
 * 모든 필드 optional - null이면 해당 필드 업데이트 안 함
 *
 * gender는 문자열("M"/"F"/"N")로 받아서 User.Gender enum으로 변환
 * 잘못된 값이면 IllegalArgumentException -> GlobalExceptionHandler가 400으로 처리
 */
public record UpdateProfileRequest(
        String nickname,    // optional, 1~30자
        Integer birthYear,  // optional
        String gender       // optional, "M" / "F" / "N"
) {
    /**
     * 편의 메서드: gender 문자열 -> User.Gender enum 변환
     * null이면 null 반환 (updateProfile에서 null은 "변경 없음"으로 처리)
     */
    public Gender genderEnum() {
        if (gender == null) return null;
        return Gender.valueOf(gender); // 잘못된 값 -> IllegalArgumentException
    }
}
