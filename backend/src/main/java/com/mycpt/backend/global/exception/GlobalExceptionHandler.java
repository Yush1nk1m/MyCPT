package com.mycpt.backend.global.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
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
 * <p>
 * 응답 형식은 기존 401 응답 바디({ "code": "...", "message": "..." })와 통일
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * DISC 원점수 검증 실패 -> 400 Bad Request
     * <p>
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

    /**
     * 타인 평정 토큰 만료 -> 400 Bad Request
     * api-design.md: 400 EXPIRED_CODE
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleTokenExpired(TokenExpiredException e) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", "EXPIRED_CODE");
        body.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 타인 평정 토큰 중복 사용 -> 400 Bad Request
     * api-design.md: 400 TOKEN_USED
     */
    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<Map<String, String>> handleTokenAlreadyUsed(TokenAlreadyUsedException e) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", "TOKEN_USED");
        body.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 프로필 이미지 검증 실패 (파일 없음 / 형식 오류 / 크기 초과) -> 400
     * UpdateProfileRequest.genderEnum() 의 잘못된 enum 값도 여기서 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", "INVALID_REQUEST");
        body.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 본인 소유가 아닌 리소스 접근 -> 403 Forbidden
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException e) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", "FORBIDDEN");
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * 리소스 없음 -> 404 Not Found
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException e) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", "NOT_FOUND");
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
