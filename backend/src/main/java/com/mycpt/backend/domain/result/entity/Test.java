package com.mycpt.backend.domain.result.entity;

import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * tests 테이블 엔티티 - 검사 응시 헤더 (Class Table Inheritance 부모)
 *
 * 설계:
 *  - rater_type=SELF:  자기 평정   (POST /results - 회원)
 *  - rater_type=OTHER: 타인 평정   (POST /assessments/{token}/submit - 비회원 가능)
 *  - label: 타인 평정 식별 라벨. assessment_tokens.label에서 복사. 자기 평정은 null
 */
@Entity
@Table(name = "tests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 검사 결과가 귀속되는 사용자 (피평정자)
    // 타인 평정이라도 이 필드는 평정 대상자(subject) 지시
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // DiscResult 역방향 참조 - Fetch Join 용도
    // cascade/orphanRemoval 없음: Test 삭제 시 disc_results는 FK CASCADE로 DB가 처리
    @OneToOne(mappedBy = "test", fetch = FetchType.LAZY)
    private DiscResult discResult;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('SELF', 'OTHER')")
    private RaterType raterType;

    @Column(nullable = false, length = 20)
    private String testType;

    // 타인 평정 식별 라벨 (nullable)
    @Column(length = 30)
    private String label;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 자기 평정 생성 팩토리 (POST /results)
     */
    public static Test createForSelf(User user, String testType) {
        Test t = new Test();
        t.user = user;
        t.raterType = RaterType.SELF;
        t.testType = testType;
        t.label = null;
        t.createdAt = LocalDateTime.now();
        return t;
    }

    /**
     * 타인 평정 생성 팩토리 (POST /assessments/{token}/submit)
     *
     * @param subject  피평정자 (링크를 생성한 회원)
     * @param testType 검사 유형 (예: "DISC")
     * @param label    평정자 식별 라벨 (assessment_tokens.label 에서 복사)
     */
    public static Test createForOther(User subject, String testType, String label) {
        Test t = new Test();
        t.user = subject;
        t.raterType = RaterType.OTHER;
        t.testType = testType;
        t.label = label;
        t.createdAt = LocalDateTime.now();
        return t;
    }
}
