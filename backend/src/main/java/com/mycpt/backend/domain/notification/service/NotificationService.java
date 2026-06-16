package com.mycpt.backend.domain.notification.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.notification.dto.NotificationListResponse;
import com.mycpt.backend.domain.notification.dto.NotificationResponse;
import com.mycpt.backend.domain.notification.entity.ChemistryNotification;
import com.mycpt.backend.domain.notification.entity.ColleagueNotification;
import com.mycpt.backend.domain.notification.entity.Notification;
import com.mycpt.backend.domain.notification.repository.NotificationRepository;
import com.mycpt.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 동료 등록 알림 전송
     * ColleagueService.register() 트랜잭션 내에서 호출됨
     */
    @Transactional
    public void sendColleagueNotification(User inviter, Colleague colleague) {
        ColleagueNotification notification = ColleagueNotification.create(inviter, colleague);
        notificationRepository.save(notification);
        // TODO: SSE 연결 중이면 즉시 푸시 (SseService 구현 후 연동)
    }

    /**
     * 케미 보고서 완료 알림 전송 (상대방 수신)
     * ChemistryLlmService 완료 후 호출됨
     */
    @Transactional
    public void sendChemistryNotification(User recipient, ChemistryReport report, User requester) {
        ChemistryNotification notification = ChemistryNotification.create(recipient, report, requester);
        notificationRepository.save(notification);
        // TODO: SSE 연결 중이면 즉시 푸시 (SseService 구현 후 연동)
    }

    /**
     * GET /notifications - 내 알림 목록
     */
    @Transactional(readOnly = true)
    public NotificationListResponse list(Long userId) {
        return new NotificationListResponse(
                notificationRepository.findAllByUserId(userId)
                        .stream()
                        .map(NotificationResponse::from)
                        .toList()
        );
    }

    /**
     * DELETE /notifications/{id} - 알림 클릭 시 삭제
     */
    @Transactional
    public void delete(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        notificationRepository.delete(notification);
    }
}
