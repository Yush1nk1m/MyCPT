package com.mycpt.backend.domain.chemistry.repository;

import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChemistryReportRepository extends JpaRepository<ChemistryReport, Long> {

    /**
     * 케미 보고서 이력 조회 - 커서 기반 페이지네이션
     *
     * - ERROR 상태는 목록에서 제외 (사용자에게 미노출)
     * - partnerId(상대방 userId) 필터: null이면 전체
     * - cursor: null이면 최신부터
     */
    @Query("""
        SELECT cr FROM ChemistryReport cr
        WHERE (cr.requester.id = :userId OR cr.partner.id = :userId)
            AND cr.status != :errorStatus
            AND (:partnerId IS NULL
                OR cr.requester.id = :partnerId
                OR cr.partner.id = :partnerId)
            AND (:cursor IS NULL OR cr.id < :cursor)
        ORDER BY cr.id DESC
    """)
    List<ChemistryReport> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("partnerId") Long partnerId,
            @Param("errorStatus")ChemistryReportStatus errorStatus,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    /**
     * 케미 보고서 상세 조회 - requester, partner EAGER 로드
     * <p>
     * requester/partner 닉네임을 응답에 포함해야 하므로 JOIN FETCH
     */
    @Query("""
        SELECT cr FROM ChemistryReport cr
        JOIN FETCH cr.requester
        JOIN FETCH cr.partner
        WHERE cr.id = :id
    """)
    Optional<ChemistryReport> findByIdWithUsers(@Param("id") Long id);

    List<ChemistryReport> findByRequesterId(@Param("requesterId") Long requesterId);
}
