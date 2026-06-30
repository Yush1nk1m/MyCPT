package com.mycpt.backend.domain.notification.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.notification.dto.NotificationListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림", description = "인앱 알림 조회/삭제")
public interface NotificationApi {

    @Operation(
            summary = "SSE 연결",
            description = "SSE 연결을 수립한다. 케미 보고서 완료·동료 등록 등 실시간 알림을 수신한다. " +
                    "재연결 시 Last-Event-ID 이후 미확인 알림 재전송(TODO).",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    SseEmitter stream(
            @AuthenticationPrincipal UserPrincipal principal
    );

    @Operation(
            summary = "알림 목록 조회",
            description = "미확인 알림 목록을 최신순으로 반환한다. 알림 없으면 빈 배열 반환.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    ResponseEntity<NotificationListResponse> list(
            @AuthenticationPrincipal UserPrincipal principal
    );

    @Operation(
            summary = "알림 삭제",
            description = "알림 클릭 시 즉시 삭제한다. 본인 알림이 아닌 경우 403.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    );
}
