package com.mycpt.backend.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 *
 * @RestControllerAdvice: 모든 @RestController에서 발생하는 예외를 중앙에서 처리
 * 예외 유형별로 @ExceptionHandler 메서드를 추가하여 도메인별 예외를 확장
 *
 * 응답 형식은 기존 401 응답 바디({ "code": "...", "message": "..." })와 통일
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * DISC 원점수 검증 실패 -> 400 Bad Request
     *
     * 응답 바디:
     * {
     * "code": "INVALID_SCORE",
     * "message": "D+I+S+C 합계는 24여야 합니다. 입력 값: 25"
     * }
     */
    @ExceptionHandler(InvalidScoreException.class)
    public ResponseEntity<Map<String, String>> handleInvalidScore(InvalidScoreException e) {
        // LinkedHashMap: 삽입 순서 보장 -> 응답 JSON 필드 순서를 code, message로 고정
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", "INVALID_SCORE");
        body.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
}
