package com.mycpt.backend.domain.result.repository;

import com.mycpt.backend.domain.result.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Long> {
    // TODO: 3주차에 이력 조회 등 확장
}
