package com.mycpt.backend.global.exception;

/**
 * 타인 평정 토큰이 만료된 경우 발생
 * GlobalExceptionHandler -> 400 Bad Request { code: "EXPIRED_CODE" }
 */
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("만료된 평정 링크입니다.");
    }
}
