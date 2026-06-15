package com.mycpt.backend.domain.result.entity;

import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * tests 테이블 엔티티 — @Inheritance(JOINED) 추상 부모
 *
 * 설계:
 *  - dtype: JPA @DiscriminatorColumn. 코드가 유효 값의 범위 보장 (기존 testType VARCHAR 대체)
 *  - rater_type=SELF:  자기 평정   (POST /results)
 *  - rater_type=OTHER: 타인 평정   (POST /assessments/{token}/submit)
 *  - label: 타인 평정 식별 라벨. assessment_tokens.label 에서 복사. 자기 평정은 null
 *  - discTest 역방향 참조 제거: 부모가 자식을 알지 못하도록 OCP 준수
 *    → DiscTest 조회는 DiscTestRepository에서 직접 수행
 */
@Entity
@Table(name = "tests")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결과가 귀속되는 사용자 (피평정자)
    // 타인 평정이라도 이 필드는 평정 대상자(subject)를 가리킴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('SELF', 'OTHER')")
    private RaterType raterType;

    // 타인 평정 식별 라벨 (nullable)
    @Column(length = 30)
    private String label;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 서브클래스 생성자에서 super(user, raterType, label) 호출
    protected Test(User user, RaterType raterType, String label) {
        this.user = user;
        this.raterType = raterType;
        this.label = label;
        this.createdAt = LocalDateTime.now();
    }
}