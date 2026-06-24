package com.mycpt.backend.domain.result.entity;

import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * disc_tests 테이블 엔티티 — Test @Inheritance(JOINED) 자식
 *
 * 설계:
 *  - @DiscriminatorValue("DISC"): tests.dtype = 'DISC' 로 저장
 *  - JPA가 tests + disc_tests 두 테이블에 나눠 INSERT/SELECT
 *  - disc_cache FK: (d_bucket, i_bucket, s_bucket, c_bucket) → disc_cache(d, i, s, c)
 *    → 81개 행은 schema.sql 시드로 사전 삽입되어 FK 위반 없음
 *  - columnDefinition = "TINYINT": Hibernate의 TINYINT → BIT 잘못 매핑 방지
 */
@Entity
@Table(name = "disc_tests")
@DiscriminatorValue("DISC")
@PrimaryKeyJoinColumn(name = "test_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscTest extends Test {

    // 원점수 (-24 ~ +48)
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int dScore;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int iScore;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int sScore;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int cScore;

    // 버킷값 (1~3) — disc_cache 복합 FK 구성
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "d", column = @Column(name = "d_bucket", nullable = false, columnDefinition = "TINYINT")),
            @AttributeOverride(name = "i", column = @Column(name = "i_bucket", nullable = false, columnDefinition = "TINYINT")),
            @AttributeOverride(name = "s", column = @Column(name = "s_bucket", nullable = false, columnDefinition = "TINYINT")),
            @AttributeOverride(name = "c", column = @Column(name = "c_bucket", nullable = false, columnDefinition = "TINYINT")),
    })
    private DiscCacheId cacheId;

    private DiscTest(User user, RaterType raterType, String label,
                     int dScore, int iScore, int sScore, int cScore,
                     int dBucket, int iBucket, int sBucket, int cBucket) {
        super(user, raterType, label);
        this.dScore = dScore;
        this.iScore = iScore;
        this.sScore = sScore;
        this.cScore = cScore;
        this.cacheId = new DiscCacheId(dBucket, iBucket, sBucket, cBucket);
    }

    /**
     * 자기 평정 생성 팩토리 (POST /results)
     */
    public static DiscTest createForSelf(User user,
                                         int dScore, int iScore, int sScore, int cScore,
                                         int dBucket, int iBucket, int sBucket, int cBucket) {
        return new DiscTest(user, RaterType.SELF, null,
                dScore, iScore, sScore, cScore,
                dBucket, iBucket, sBucket, cBucket);
    }

    /**
     * 타인 평정 생성 팩토리 (POST /assessments/{token}/submit)
     *
     * @param subject 피평정자 (링크를 생성한 회원)
     * @param label   평정자 식별 라벨 (assessment_tokens.label 에서 복사)
     */
    public static DiscTest createForOther(User subject, String label,
                                          int dScore, int iScore, int sScore, int cScore,
                                          int dBucket, int iBucket, int sBucket, int cBucket) {
        return new DiscTest(subject, RaterType.OTHER, label,
                dScore, iScore, sScore, cScore,
                dBucket, iBucket, sBucket, cBucket);
    }
}