package com.mycpt.backend.domain.colleague.repository;

import com.mycpt.backend.domain.colleague.entity.Colleague;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ColleagueRepository extends JpaRepository<Colleague, Long> {

    // 동료 목록 조회 - UNION ALL로 user_a / user_b 양방향 포함
    // JOIN FETCH로 N+1 방지 (목록에서 파트너 닉네임/이미지 즉시 필요)
    @Query("""
        SELECT c FROM Colleague c
        JOIN FETCH c.userA
        JOIN FETCH c.userB
        WHERE c.userA.id = :userId OR c.userB.id = :userId
        ORDER BY c.createdAt DESC
    """)
    List<Colleague> findAllByUserId(@Param("userId") Long userId);

    // 특정 동료 관계 조회 (권한 검증용 - 내가 포함된 관계인지)
    // user_a_id < user_b_id 정렬 없이 OR 조건으로 조회
    @Query("""
        SELECT c FROM Colleague c
        JOIN FETCH c.userA
        JOIN FETCH c.userB
        WHERE c.id = :colleagueId
            AND (c.userA.id = :userId OR c.userB.id = :userId)
    """)
    Optional<Colleague> findByIdAndUserId(
            @Param("colleagueId") Long colleagueId,
            @Param("userId") Long userId
    );

    @Query("""
        SELECT c FROM Colleague c
        JOIN FETCH c.userA
        JOIN FETCH c.userB
        WHERE c.userA.id = :userAId AND c.userB.id = :userBId
    """)
    Optional<Colleague> findByPair(
            @Param("userAId") Long userAId,
            @Param("userBId") Long userBId
    );

    // 이미 동료인지 확인 (user_a_id < user_b_id 정렬 적용)
    @Query("""
        SELECT COUNT(c) > 0 FROM Colleague c
        WHERE c.userA.id = :userAId AND c.userB.id = :userBId
    """)
    boolean existsByPair(
        @Param("userAId") Long userAId,
        @Param("userBId") Long userBId
    );

    // 동료 삭제용 - 권한 포함 조회는 서비스에서 findByIdAndUserId로 처리 후 삭제
    // (별도 DELETE 쿼리 불필요 - JpaRepository.delete() 사용)
}
