package com.mycpt.backend.domain.chemistry.repository;

import com.mycpt.backend.domain.chemistry.entity.ChemistryCache;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChemistryCacheRepository
        extends JpaRepository<ChemistryCache, ChemistryCacheId> {
}
