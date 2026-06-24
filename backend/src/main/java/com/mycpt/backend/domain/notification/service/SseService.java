package com.mycpt.backend.domain.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 연결 관리 서비스.
 *
 * Map<userId, SseEmitter> 소유 — 연결 수립/해제/이벤트 push 담당.
 * chemistry 도메인을 몰라도 됨. userId + 이벤트 타입 + 페이로드만 받아서 push.
 *
 * Last-Event-ID 기반 재전송:
 *   재연결 시 Last-Event-ID 이후 미확인 알림을 notifications 테이블에서 조회해 push.
 *   (NotificationService와 협력)
 */
@Slf4j
@Service
public class SseService {

    // 연결 유지 타임아웃: 30분. 클라이언트는 연결 끊기면 즉시 재연결.
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE 연결 수립. GET /notifications/stream 진입점
     */
    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        emitters.put(userId, emitter);
        log.debug("SSE 연결. userId={}", userId);

        // 연결 직후 더미 이벤트 - 일부 브라우저에서 연결 확인 용도
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 케미 보고서 생성 완료 push
     * ChemistryEventSubscriber가 Pub/Sub 수신 후 호출
     */
    public void pushChemistryReady(Long userId, Long reportId) {
        push(userId, "CHEMISTRY_REPORT", reportId, "케미 보고서가 발행됐어요.");
    }

    /**
     * 케미 보고서 생성 실패 push
     */
    public void pushChemistryError(Long userId, Long reportId) {
        push(userId, "CHEMISTRY_ERROR", reportId, "보고서 생성에 실패했어요. 코인이 환불됐어요.");
    }

    /**
     * 동료 등록 알림 push
     * NotificationService.sendColleagueNotification() 완료 후 호출
     */
    public void pushColleagueRegistered(Long userId, Long colleagueId) {
        push(userId, "COLLEAGUE_REGISTERED", colleagueId, "새 동료가 등록되었어요.");
    }

    private void push(Long userId, String eventType, Long referenceId, String message) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            // SSE 미연결 상태 - 인앱 알림 테이블에는 이미 저장됐으므로 무시
            log.debug("SSE 미연결. push 생략. userId={}, eventType={}", userId, eventType);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(referenceId))
                    .name(eventType)
                    .data("{\"referenceId\":" + referenceId + ",\"message\":\"" + message + "\"}"));
            log.debug("SSE push 완료. userId={}, eventType={}", userId, eventType);
        } catch (IOException e) {
            emitters.remove(userId);
            emitter.completeWithError(e);
            log.warn("SSE push 실패. 연결 제거. userId={}", userId);
        }
    }
}
