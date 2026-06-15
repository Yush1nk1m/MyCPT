package com.mycpt.backend.domain.notification.entity;

import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인앱 알림 공통 헤더 (notifications 테이블)
 *
 * 설계:
 *  - @Inheritance(JOINED) 추상 부모 — 알림 유형별 자식 테이블이 FK로 참조
 *  - dtype: @DiscriminatorColumn — COLLEAGUE_REGISTERED / CHEMISTRY_REPORT
 *  - 클릭 시 즉시 DELETE — 읽음 처리 없음
 *  - 자식이 referenceId를 직접 보유 (FK 보장) → 부모는 공통 필드만 관리
 */
@Entity
@Table(name = "notifications")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Notification(User user, String message) {
        this.user = user;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
