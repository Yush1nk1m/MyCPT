package com.mycpt.backend.common.exception;

import lombok.Getter;

/**
 * 서비스 비즈니스 예외 베이스 클래스
 *
 * 도메인별 개별 예외 클래스 대신 단일 클래스로 모든 비즈니스 예외를 표현한다.
 * ErrorCode가 HTTP 상태 코드와 기본 메시지를 결정하며, 생성자 오버로드로 메시지 커스터마이징이 가능하다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 메시지 사용
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    // 메시지 커스터마이징
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
