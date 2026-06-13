package com.mycpt.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전체 에러 코드 열거형
 *
 * api-design.md에 정의된 모든 에러 코드를 관리한다.
 * 새 에러가 필요하면 이 enum에 추가한다.
 *
 * 구조: code(응답 JSON의 code 필드), status(HTTP 상태 코드), message(기본 메시지)
 */
@Getter
public enum ErrorCode {

    // ── 400 Bad Request ──────────────────────────────────────────────────────
    INVALID_SCORE(HttpStatus.BAD_REQUEST, "원점수 검증에 실패했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    EXPIRED_CODE(HttpStatus.BAD_REQUEST, "만료된 코드입니다."),
    TOKEN_USED(HttpStatus.BAD_REQUEST, "이미 사용된 토큰입니다."),
    SELF_INVITE(HttpStatus.BAD_REQUEST, "본인의 초대 코드는 사용할 수 없습니다."),
    INSUFFICIENT_COINS(HttpStatus.BAD_REQUEST, "코인이 부족합니다."),

    // ── 403 Forbidden ────────────────────────────────────────────────────────
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // ── 404 Not Found ────────────────────────────────────────────────────────
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),

    // ── 409 Conflict ─────────────────────────────────────────────────────────
    ALREADY_COLLEAGUE(HttpStatus.CONFLICT, "이미 동료로 등록된 사용자입니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
