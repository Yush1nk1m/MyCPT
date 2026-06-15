package com.mycpt.backend.domain.notification.entity;

import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 케미 보고서 알림 (chemistry_notifications 테이블)
 *
 * 설계:
 *  - notifications JOINED 상속 자식
 *  - chemistry_report_id → chemistry_reports.id FK로 타입 안전성 확보
 */
@Entity
@Table(name = "chemistry_notifications")
@DiscriminatorValue("CHEMISTRY_REPORT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChemistryNotification extends Notification {

    // FK -> chemistry_reports.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chemistry_report_id", nullable = false)
    private ChemistryReport chemistryReport;

    private ChemistryNotification(User recipient, ChemistryReport report, String message) {
        super(recipient, message);
        this.chemistryReport = report;
    }

    public static ChemistryNotification create(User recipient, ChemistryReport report, User requester) {
        String message = requester.getNickname() + "님과의 케미 보고서가 발행됐어요.";
        return new ChemistryNotification(recipient, report, message);
    }
}
