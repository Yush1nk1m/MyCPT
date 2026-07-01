package com.mycpt.backend.domain.chemistry.repository;

import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChemistryCacheRepository
        extends JpaRepository<ChemistryCache, ChemistryCacheId> {

    /**
     * SELECT FOR UPDATE - 발행자/구독자 결정용 비관적 락
     * <p>
     * 트랜잭션 커밋 시 락 즉시 해제
     * 호출 측에서 반드시 @Transactional(독립 트랜잭션) 내에서 실행해야 함
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChemistryCache c WHERE c.id = :id")
    Optional<ChemistryCache> findByIdWithLock(@Param("id") ChemistryCacheId id);

    List<ChemistryCache> findByStatusAndUpdatedAtBefore(ChemistryCacheStatus status, LocalDateTime cutoff);
}
