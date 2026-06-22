package com.mycpt.backend.domain.chemistry.entity;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    // GENERATING / READY / ERROR
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChemistryReportStatus status;

    // nullable: @Async 발행 중 null, LLM 완료 후 UPDATE
    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // POST /chemistry-reports 202 반환 직전 호출
    // status = GENERATING, report = null로 행 선점
    public static ChemistryReport create(User requester, User partner, TestType testType) {
        ChemistryReport cr = new ChemistryReport();
        cr.requester = requester;
        cr.partner = partner;
        cr.testType = testType;
        cr.status = ChemistryReportStatus.GENERATING;
        cr.report = null;
        cr.createdAt = LocalDateTime.now();
        return cr;
    }

    // LLM 보고서 생성 완료 후 ChemistryLlmService에서 호출
    public void complete(String report) {
        this.status = ChemistryReportStatus.READY;
        this.report = report;
    }

    // LLM 실패(@Retryable 소진) 후 ChemistryLlmService에서 호출
    public void fail() {
        this.status = ChemistryReportStatus.ERROR;
    }
}
