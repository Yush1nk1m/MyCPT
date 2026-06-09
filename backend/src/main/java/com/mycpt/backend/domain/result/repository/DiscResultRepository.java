package com.mycpt.backend.domain.result.repository;

import com.mycpt.backend.domain.result.entity.DiscResult;
import com.mycpt.backend.domain.result.enums.RaterType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiscResultRepository extends JpaRepository<DiscResult, Long> {

    /**
     * 결과 이력 목록 조회 - 커서 기반 페이지네이션
     * <p>
     * - DiscResult가 연관관계의 주인(@MapsId)이므로 이 방향에서 JOIN FETCH 해야 실제 LAZY 동작
     * - Pageable로 LIMIT 적용 (PageRequest.of(0, size + 1) 전달 - OFFSET 없이 LIMIT만 사용)
     * - size + 1 조회 후 서비스에서 hasNext 판단
     * - raterType == null이면 전체 조회
     */
    @Query("""
        SELECT dr FROM DiscResult dr
        JOIN FETCH dr.test t
        WHERE t.user.id = :userId
            AND (:raterType IS NULL OR t.raterType = :raterType)
            AND (:cursor IS NULL OR t.id < :cursor)
        ORDER BY t.id DESC
    """)
    List<DiscResult> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("raterType") RaterType raterType,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    /**
     * 결과 상세 조회 - test JOIN FETCH
     *
     * 본인 소유 검증은 서비스 레이어에서 수행 (userId 비교)
     * report는 서비스에서 CacheService 경유로 별도 조회 (엔티티 복잡성 감소를 위한 설계)
     */
    @Query("""
        SELECT dr FROM DiscResult dr
        JOIN FETCH dr.test t
        WHERE t.id = :testId
    """)
    Optional<DiscResult> findByTestIdWithDetail(@Param("testId") Long testId);
}
