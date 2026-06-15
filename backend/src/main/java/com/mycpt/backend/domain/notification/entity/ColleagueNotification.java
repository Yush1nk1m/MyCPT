package com.mycpt.backend.domain.notification.entity;

import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 동료 등록 알림 (colleague_notifications 테이블)
 *
 * 설계:
 *  - notifications JOINED 상속 자식
 *  - colleague_id → colleagues.id FK로 타입 안전성 확보
 */
@Entity
@Table(name = "colleague_notifications")
@DiscriminatorValue("COLLEAGUE_REGISTERED")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ColleagueNotification extends Notification {

    // FK -> colleagues.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colleague_id", nullable = false)
    private Colleague colleague;

    private ColleagueNotification(User recipient, Colleague colleague, String message) {
        super(recipient, message);
        this.colleague = colleague;
    }

    public static ColleagueNotification create(User recipient, Colleague colleague) {
        // 상대방 닉네임을 메시지에 포함
        User requester = colleague.getPartner(recipient.getId());
        String message = requester.getNickname() + "님이 동료로 등록했어요.";
        return new ColleagueNotification(recipient, colleague, message);
    }
}
