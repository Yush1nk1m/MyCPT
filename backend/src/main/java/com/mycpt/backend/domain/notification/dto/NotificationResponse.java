package com.mycpt.backend.domain.notification.dto;

import com.mycpt.backend.domain.notification.entity.ChemistryNotification;
import com.mycpt.backend.domain.notification.entity.ColleagueNotification;
import com.mycpt.backend.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        String type,
        Long referenceId,
        String message,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        String type;
        Long referenceId;

        if (notification instanceof ColleagueNotification cn) {
            type = "COLLEAGUE_REGISTERED";
            referenceId = cn.getColleague().getId();
        } else if (notification instanceof ChemistryNotification cn) {
            type = "CHEMISTRY_REPORT";
            referenceId = cn.getChemistryReport().getId();
        } else {
            // 신규 알림 타입 추가 시 분기 업데이트
            throw new IllegalStateException("Unknown notification type: " + notification.getClass());
        }

        return new NotificationResponse(
                notification.getId(),
                type,
                referenceId,
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}
