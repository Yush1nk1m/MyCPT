package com.mycpt.backend.domain.result.repository;

import com.mycpt.backend.domain.result.entity.DiscResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscResultRepository extends JpaRepository<DiscResult, Long> {
    // TODO: 3주차에 결과 상세 조회 등 확장
}
