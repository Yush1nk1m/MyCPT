package com.mycpt.backend.global.exception;

/**
 * DISC 원점수 검증 실패 시 발생하는 예외.
 *
 * 발생 조건:
 *   1. 개별 원점수가 허용 범위(-24 ~ +48)를 벗어난 경우
 *   2. D + I + S + C 합계가 24가 아닌 경우
 *
 * GlobalExceptionHandler가 이 예외를 잡아 400 Bad Request로 변환한다.
 */
public class InvalidScoreException extends RuntimeException {
    public InvalidScoreException(String message) {
        super(message);
    }
}
