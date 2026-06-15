package com.mycpt.backend.domain.notification.entity;

import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인앱 알림 (notifications 테이블)
 *
 * 설계:
 *  - 클릭 시 즉시 DELETE - 읽음 처리 없음
 *  - reference_id: COLLEAGUE_REGISTERED -> colleagues.id / CHEMISTRY_REPORT -> chemistry_reports.id
 */
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    public enum Type {
        CHEMISTRY_REPORT,
        DOLLEAGUE_REGISTERED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    // 관련 엔티티 id (colleagues.id 또는 chemistry_reports.id)
    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Notification create(User recipient, Type type, Long referenceId, String message) {
        Notification n = new Notification();
        n.user = recipient;
        n.type = type;
        n.referenceId = referenceId;
        n.message = message;
        n.createdAt = LocalDateTime.now();
        return n;
    }
}
