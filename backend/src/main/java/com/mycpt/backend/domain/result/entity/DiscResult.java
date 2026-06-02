package com.mycpt.backend.domain.result.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * disc_results 테이블 엔티티 - DISC 검사 전용 결과 (Class Table Inheritance 자식)
 *
 * 설계:
 *  - test_id가 PK 겸 FK -> tests.id (1:1 관계 스키마 레벨 강제)
 *  - (d_bucket, i_bucket, s_bucket, c_bucket) -> disc_cache 복합 FK
 *  - columnDefinition = "TINYINT": Hibernate가 TINYINT를 BIT로 매핑하는 버그 방지
 */
@Entity
@Table(name = "disc_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscResult {

    // test_id를 PK로 사용 - @GeneratedValue 없음 (Test.id를 그대로 사용)
    @Id
    private Long testId;

    // tests와의 1:1 관계
    // @MapsId: test.id 값을 testId PK에 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "test_id")
    private Test test;

    // 원점수 (-24 ~ +48)
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int dScore;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int iScore;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int sScore;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int cScore;

    // 버킷 값 (1~3) - disc_cache 복합 FK
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int dBucket;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int iBucket;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int sBucket;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int cBucket;

    /**
     * 정적 팩토리 메서드
     *
     * disc_cache FK 제약: (d_bucket, i_bucket, s_bucket, c_bucket)에 해당하는 disc_cache 행이 미리 존재해야 함
     * submit 시점에서는 캐시가 없을 수 있으므로 AssessmentService.submit()에서 이 점을 주석으로 명시함
     */
    public static DiscResult create(
            Test test,
            int dScore, int iScore, int sScore, int cScore,
            int dBucket, int iBucket, int sBucket, int cBucket
    ) {
        DiscResult r = new DiscResult();
        r.test = test;
        r.dScore = dScore;
        r.iScore = iScore;
        r.sScore = sScore;
        r.cScore = cScore;
        r.dBucket = dBucket;
        r.iBucket = iBucket;
        r.sBucket = sBucket;
        r.cBucket = cBucket;
        return r;
    }
}
