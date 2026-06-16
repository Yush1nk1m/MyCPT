package com.mycpt.backend.domain.chemistry.entity;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 케미 보고서 (chemistry_reports 테이블)
 *
 * 현재 상태: 스텁 — ChemistryNotification 컴파일 의존성 해소 목적.
 * 비즈니스 로직(발행, LLM 호출)은 Chemistry 도메인 구현 시 추가.
 *
 * 설계:
 *  - testType: common.enums.TestType — DTO에는 사용하지 않음 (점수 구조가 유형마다 달라 혼용 불가)
 *  - report: nullable — @Async 발행 중 null, 완료 후 TEXT 저장
 *    schema.sql의 NOT NULL은 Chemistry 구현 시 nullable로 변경 필요
 */
@Entity
@Table(name = "chemistry_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChemistryReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private User partner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TestType testType;

    // nullable: @Async 발행 중 null, LLM 완료 후 UPDATE
    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
