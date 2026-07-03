package com.mycpt.backend.domain.colleague.repository;

import com.mycpt.backend.domain.colleague.entity.PeerCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PeerCodeRepository extends JpaRepository<PeerCode, Long> {

    Optional<PeerCode> findByCode(String code);

    /**
     * userId로 본인 코드 조회
     * user_id UNIQUE 제약으로 0~1건만 존재
     */
    Optional<PeerCode> findByUserId(Long userId);

    /**
     * 코드 문자열로 조회 - 동료 등록 시 초대자 확인 용도
     */
    @Query("SELECT pc FROM PeerCode pc JOIN FETCH pc.user u WHERE pc.code = :code")
    Optional<PeerCode> findByCodeWithUser(@Param("code") String code);

    boolean existsByCode(String code);

    long deleteByExpiresAtBefore(LocalDateTime cutoff);
}
