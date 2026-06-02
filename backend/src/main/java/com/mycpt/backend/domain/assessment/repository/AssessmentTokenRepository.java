package com.mycpt.backend.domain.assessment.repository;

import com.mycpt.backend.domain.assessment.entity.AssessmentToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssessmentTokenRepository extends JpaRepository<AssessmentToken, Long> {

    // 토큰 문자열로 단건 조회 - GET /assessments/{token}, POST /assessments/{token}/submit
    Optional<AssessmentToken> findByToken(String token);
}
