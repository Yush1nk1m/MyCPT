package com.mycpt.backend.domain.coin.repository;

import com.mycpt.backend.domain.coin.entity.CoinTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {

    // 커서 기반 페이지네이션: cursor(이전 응답의 마지막 id)보다 작은 행을 최신순으로 size+1개 조회
    // size+1개를 가져와서 hasNext 판단에 사용 (서비스 레이어에서 처리)
    @Query("""
        SELECT ct FROM CoinTransaction ct
        WHERE ct.user.id = :userId
            AND (:cursor IS NULL OR ct.id < :cursor)
        ORDER BY ct.id DESC
    """)
    List<CoinTransaction> findHistory(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
