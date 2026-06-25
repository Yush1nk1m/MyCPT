package com.mycpt.backend.domain.statistics.repository;

import com.mycpt.backend.domain.result.entity.DiscTest;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.statistics.dto.BucketAverage;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import com.mycpt.backend.domain.statistics.dto.TrendPoint;
import com.mycpt.backend.domain.user.enums.Gender;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<DiscTest, Long> {

    /**
     * [comparison] 본인 최신 자기 평정 버킷 값 1건 조회
     *
     * Pageable(0, 1)로 LIMIT 1 적용
     * List<>로 받아서 empty -> my.buckets = null 처리
     */
    @Query("""
        SELECT dt.cacheId.d AS dBucket, dt.cacheId.i AS iBucket,
               dt.cacheId.s AS sBucket, dt.cacheId.c AS cBucket
        FROM DiscTest dt
        WHERE dt.user.id = :userId
            AND dt.raterType = :raterType
        ORDER BY dt.id DESC
    """)
    List<LatestBuckets> findLatestBuckets(
            @Param("userId") Long userId,
            @Param("raterType") RaterType raterType,
            Pageable pageable
    );

    /**
     * [comparison] 나이대/성별 자기 평정 평균 집계
     *
     * birthYear BETWEEN :from AND :to 로 나이대 필터
     * 결과 없을 시 AVG=null, COUNT=0 -> Optional.empty() 처리
     */
    @Query("""
        SELECT new com.mycpt.backend.domain.statistics.dto.BucketAverage(
            AVG(dt.cacheId.d), AVG(dt.cacheId.i), AVG(dt.cacheId.s), AVG(dt.cacheId.c), COUNT(dt)
        )
        FROM DiscTest dt
        JOIN dt.user u
        WHERE dt.raterType = :raterType
            AND u.birthYear BETWEEN :birthYearFrom AND :birthYearTo
            AND u.gender = :gender
            AND dt.id IN (
                SELECT MAX(dt2.id)
                FROM DiscTest dt2
                JOIN dt2.user u2
                WHERE dt2.raterType = :raterType
                    AND u2.birthYear BETWEEN :birthYearFrom AND :birthYearTo
                    AND u2.gender = :gender
                GROUP BY dt2.user.id
            )
    """)
    Optional<BucketAverage> findAverageBuckets(
            @Param("raterType") RaterType raterType,
            @Param("birthYearFrom") int birthYearFrom,
            @Param("birthYearTo") int birthYearTo,
            @Param("gender") Gender gender
    );

    /**
     * [trend] 자기 평정 변화 추이 (기간 필터, 오름차순)
     */
    @Query("""
        SELECT new com.mycpt.backend.domain.statistics.dto.TrendPoint(
            dt.cacheId.d, dt.cacheId.i, dt.cacheId.s, dt.cacheId.c, dt.createdAt
        )
        FROM DiscTest dt
        WHERE dt.user.id = :userId
            AND dt.raterType = :raterType
            AND dt.createdAt >= :from
        ORDER BY dt.id ASC
    """)
    List<TrendPoint> findTrendBuckets(
            @Param("userId") Long userId,
            @Param("raterType") RaterType raterType,
            @Param("from") LocalDateTime from
    );
}
