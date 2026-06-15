package com.mycpt.backend.domain.notification.repository;

import com.mycpt.backend.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자 알림 목록 - 최신순, 부모 타입으로 다형 조회 (JOINED 전략에 의해 자동 JOIN)
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findAllByUserId(@Param("userId") Long userId);
}
