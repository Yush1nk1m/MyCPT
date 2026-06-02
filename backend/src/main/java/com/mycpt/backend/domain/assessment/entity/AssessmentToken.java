package com.mycpt.backend.domain.assessment.entity;

import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 타인 평정 일회용 링크 토큰 (assessment_tokens 테이블)
 *
 * 생명주기:
 *  생성  ->  POST    /assessments                (회원이 링크 생성 시)
 *  검증  ->  GET     /assessments/{token}        (평정자가 링크 접속 시
 *  소비  ->  POST    /assessments/{token}/submit (평정 제출 시 used=TRUE)
 *  삭제  ->  배치 스케줄러                         (expires_at 지난 토큰 일괄 삭제)
 *
 * 설계 결정:
 *  - token: UUID 32자 (하이픈 제거). CHAR(32) UNIQUE
 *  - used=TRUE 처리로 중복 제출 방지 (행 DELETE 없이 UPDATE - 배치 통합 삭제)
 *  - label: 평정자 식별 라벨 (예: "여자친구"). submit 시 tests.label 에 복사
 */
@Entity
@Table(name = "assessment_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssessmentToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 평정 대상자 (링크를 생성한 회원)
    // 평정 결과가 이 사용자의 계정에 귀속됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private User subject;

    // 일회용 랜덤 토큰 - UUID 하이픈 제거 32자
    @Column(nullable = false, unique = true, length = 32)
    private String token;

    // 평정자 식별 라벨 (최대 30자, 선택 값)
    // submit 시 tests.label 컬럼에 복사
    @Column(length = 30)
    private String label;

    // 사용 여부 - TRUE일 시 재접속/재제출 차단
    @Column(nullable = false, columnDefinition = "TINYINT")
    private boolean used;

    // 만료 시각 - 생성 시점 +7일
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 정적 팩토리 메서드
     *
     * @param subject 평정 대상자 (링크 생성한 회원)
     * @param label   평정 식별 라벨 (nullable)
     * @param ttlDays 토큰 유효 기간 (일). 프로퍼티에서 주입, 기본 7
     */
    public static AssessmentToken create(User subject, String label, long ttlDays) {
        AssessmentToken t = new AssessmentToken();
        t.subject = subject;
        // UUID 하이픈 제거 -> 32자
        t.token = UUID.randomUUID().toString().replace("-", "");
        t.label = label;
        t.used = false;
        t.createdAt = LocalDateTime.now();
        t.expiresAt = t.createdAt.plusDays(ttlDays);
        return t;
    }

    /**
     * 평정 제출 완료 처리 - used=TRUE
     * submit() 호출 후 save() 없이도 @Transactional dirty checking으로 UPDATE
     * CacheService 패턴과 일관성을 위해 명시적 save()는 서비스 레이어에서 수행
     */
    public void markUsed() {
        this.used = true;
    }
}
