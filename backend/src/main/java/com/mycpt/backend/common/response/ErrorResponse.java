package com.mycpt.backend.common.response;

import com.mycpt.backend.common.exception.BusinessException;

/**
 * 에러 응답 DTO
 *
 * 응답 JSON 형식: { "code": "EXPIRED_CODE", "message": "만료된 코드입니다." }
 * 기존 401 응답 바디와 동일한 구조 유지
 *
 * record 사용: 불변 값 객체, getter, equals, hashCode, toString 자동 생성
 */
public record ErrorResponse(String code, String message) {

    public static ErrorResponse from(BusinessException e) {
        return new ErrorResponse(
                e.getErrorCode().name(),    // enum 이름이 그대로 code가 됨
                e.getMessage()
        );
    }
}
