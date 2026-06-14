package com.mycpt.backend.domain.statistics.repository;

import com.mycpt.backend.domain.result.entity.Test;
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

public interface StatisticsRepository extends JpaRepository<Test, Long> {

    /**
     * [comparison] 본인 최신 자기 평정 버킷 값 1건 조회
     *
     * Pageable(0, 1)로 LIMIT 1 적용
     * List<>로 받아서 empty -> my.buckets = null 처리
     */
    @Query("""
                SELECT dr.dBucket AS dBucket, dr.iBucket AS iBucket, dr.sBucket AS sBucket, dr.cBucket AS cBucket
                FROM DiscResult dr
                JOIN dr.test t
                WHERE t.user.id = :userId
                    AND t.raterType = :raterType
                ORDER BY t.id DESC
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
                    AVG(dr.dBucket), AVG(dr.iBucket), AVG(dr.sBucket), AVG(dr.cBucket), COUNT(dr)
                )
                FROM DiscResult dr
                JOIN dr.test t
                JOIN t.user u
                WHERE t.raterType = :raterType
                    AND u.birthYear BETWEEN :birthYearFrom AND :birthYearTo
                    AND u.gender = :gender
                    AND t.id IN (
                        SELECT MAX(t2.id)
                        FROM Test t2
                        JOIN t2.user u2
                        WHERE t2.raterType = :raterType
                            AND u2.birthYear BETWEEN :birthYearFrom AND :birthYearTo
                            AND u2.gender = :gender
                        GROUP BY t2.user.id
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
                    dr.dBucket, dr.iBucket, dr.sBucket, dr.cBucket, t.createdAt
                )
                FROM DiscResult dr
                JOIN dr.test t
                WHERE t.user.id = :userId
                    AND t.raterType = :raterType
                    AND t.createdAt >= :from
                ORDER BY t.id ASC
            """)
    List<TrendPoint> findTrendBuckets(
            @Param("userId") Long userId,
            @Param("raterType") RaterType raterType,
            @Param("from") LocalDateTime from
    );
}
