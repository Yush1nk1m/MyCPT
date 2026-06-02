package com.mycpt.backend.global.exception;

/**
 * 타인 평정 토큰이 이미 사용된 경우 발생
 * GlobalExceptionHandler -> 400 Bad Request { code: "TOKEN_USED" }
 */
public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException() {
        super("이미 사용된 평정 링크입니다.");
    }
}
