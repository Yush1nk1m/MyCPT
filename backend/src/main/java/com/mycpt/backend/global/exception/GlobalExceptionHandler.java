package com.mycpt.backend.global.exception;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 *
 * @RestControllerAdvice: 모든 @RestController에서 발생하는 예외를 중앙에서 처리
 * 예외 유형별로 @ExceptionHandler 메서드를 추가하여 도메인별 예외를 확장
 *
 * BusinessException 하나만 처리하면 된다.
 * ErrorCode가 HTTP 상태와 코드를 모두 결정하므로 새 에러가 생겨도 이 파일은 변경할 필요가 없다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.from(e));
    }
}
