package com.mycpt.backend.domain.notification.controller;

import com.mycpt.backend.domain.notification.dto.NotificationListResponse;
import com.mycpt.backend.domain.notification.dto.NotificationResponse;
import com.mycpt.backend.domain.notification.service.NotificationService;
import com.mycpt.backend.domain.notification.service.SseService;
import com.mycpt.backend.support.MvcTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest: Controller + Security Filter Chain만 로드. NotificationService는 @MockitoBean으로 대체.
@WebMvcTest(NotificationV1Controller.class)
@DisplayName("NotificationV1Controller 슬라이스 테스트")
class NotificationV1ControllerTest extends MvcTestSupport {

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private SseService sseService;

    // ── GET /notifications ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/notifications")
    class List_ {

        @Test
        @DisplayName("[ST-NotificationCtrl-목록조회-성공]")
        void 목록조회_성공() throws Exception {
            // given
            NotificationResponse item = new NotificationResponse(
                    10L, "COLLEAGUE_REGISTERED", 5L, "동료가 등록되었습니다.", LocalDateTime.now()
            );
            given(notificationService.list(any()))
                    .willReturn(new NotificationListResponse(List.of(item)));

            // when
            mockMvc.perform(get("/api/v1/notifications")
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notifications[0].notificationId").value(10))
                    .andExpect(jsonPath("$.notifications[0].type").value("COLLEAGUE_REGISTERED"))
                    .andExpect(jsonPath("$.notifications[0].referenceId").value(5));
        }

        @Test
        @DisplayName("[ST-NotificationCtrl-목록조회-미인증]")
        void 목록조회_미인증() throws Exception {
            // when
            mockMvc.perform(get("/api/v1/notifications"))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── DELETE /notifications/{id} ───────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/notifications/{id}")
    class Delete {

        @Test
        @DisplayName("[ST-NotificationCtrl-삭제-성공]")
        void 삭제_성공() throws Exception {
            // when
            mockMvc.perform(delete("/api/v1/notifications/{id}", 10L)
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[ST-NotificationCtrl-삭제-미인증]")
        void 삭제_미인증() throws Exception {
            // when
            mockMvc.perform(delete("/api/v1/notifications/{id}", 10L))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }
}