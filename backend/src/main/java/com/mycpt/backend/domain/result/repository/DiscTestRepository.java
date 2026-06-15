package com.mycpt.backend.domain.result.repository;

import com.mycpt.backend.domain.result.entity.DiscTest;
import com.mycpt.backend.domain.result.enums.RaterType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiscTestRepository extends JpaRepository<DiscTest, Long> {

    /**
     * 결과 이력 목록 조회 — 커서 기반 페이지네이션
     *
     * - @Inheritance(JOINED)이므로 JPA가 tests JOIN disc_tests 자동 생성
     * - Pageable로 LIMIT 적용 (PageRequest.of(0, size + 1) — OFFSET 없이 LIMIT만)
     * - size + 1 조회 후 서비스에서 hasNext 판단
     * - raterType == null이면 SELF/OTHER 전체 조회
     */
    @Query("""
        SELECT dt FROM DiscTest dt
        WHERE dt.user.id = :userId
            AND (:raterType IS NULL OR dt.raterType = :raterType)
            AND (:cursor IS NULL OR dt.id < :cursor)
        ORDER BY dt.id DESC
    """)
    List<DiscTest> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("raterType") RaterType raterType,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    /**
     * 결과 상세 조회
     *
     * - @Inheritance(JOINED)이므로 별도 JOIN FETCH 없이 tests + disc_tests 함께 로드
     * - 본인 소유 검증은 서비스 레이어에서 수행 (userId 비교)
     * - report는 서비스에서 CacheService 경유로 별도 조회
     */
    @Query("""
        SELECT dt FROM DiscTest dt
        WHERE dt.id = :testId
    """)
    Optional<DiscTest> findByTestIdWithDetail(@Param("testId") Long testId);
}
