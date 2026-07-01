package com.mycpt.backend.domain.notification.repository;

import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.notification.entity.ColleagueNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// ColleagueNotification 엔티티는 자식 클래스이기 때문에 별도 리포지토리 필요
public interface ColleagueNotificationRepository extends JpaRepository<ColleagueNotification, Long> {
    List<ColleagueNotification> findAllByColleague(Colleague colleague);
}
