package com.mycpt.backend.domain.colleague.entity;

import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 동료 관계 (colleagues 테이블)
 *
 * 설계:
 *  - user_a_id < user_b_id 규칙으로 단일 행에 양방향 관계 저장
 *  - UNIQUE(user_a_id, user_b_id)만으로 중복 방지
 *  - 목록 조회는 UNION ALL로 양방향 처리 (ColleagueRepository)
 */
@Entity
@Table(name = "colleagues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Colleague {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 두 사용자 중 ID가 작은 쪽 - 삽입 시 서비스에서 정렬
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    // 두 사용자 중 ID가 큰 쪽
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Colleague create(User userA, User userB) {
        // userA.id < userB.id 정렬은 서비스에서 보장 후 호출
        Colleague c = new Colleague();
        c.userA = userA;
        c.userB = userB;
        c.createdAt = LocalDateTime.now();
        return c;
    }

    /**
     * 본인 기준 상대방 User 반환
     */
    public User getPartner(Long myUserId) {
        return userA.getId().equals(myUserId) ? userB : userA;
    }
}
