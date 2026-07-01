package com.mycpt.backend.domain.chemistry.entity;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * chemistry_reports 테이블 엔티티.
 *
 * 사용자별 발행 요청 이력을 추적한다. API 응답에 노출되는 status가 여기 있음.
 * chemistry_cache.status(락 라이프사이클)와 별개 관심사.
 *
 * 상태 전이:
 *   create()         → status=GENERATING (chemistry_cache 락 이후 INSERT)
 *   complete(cacheId)→ status=READY, cacheId 세팅
 *   fail()           → status=ERROR
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

    // 사용자별 발행 요청 상태. API 응답에 노출.
    // chemistry_cache.status(락 라이프사이클)와 별개.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChemistryReportStatus status;

    // chemistry_cache 복합 FK - ChemistryCacheId의 8개 컬럼이 DDL에 매핑
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "requesterD", column = @Column(name = "requester_d", columnDefinition = "TINYINT")),
            @AttributeOverride(name = "requesterI", column = @Column(name = "requester_i", columnDefinition = "TINYINT")),
            @AttributeOverride(name = "requesterS", column = @Column(name = "requester_s", columnDefinition = "TINYINT")),
            @AttributeOverride(name = "requesterC", column = @Column(name = "requester_c", columnDefinition = "TINYINT")),
            @AttributeOverride(name = "partnerD", column = @Column(name = "partner_d", columnDefinition = "TINYINT")),
            @AttributeOverride(name = "partnerI", column = @Column(name = "partner_i", columnDefinition = "TINYINT")),
            @AttributeOverride(name = "partnerS", column = @Column(name = "partner_s", columnDefinition = "TINYINT")),
            @AttributeOverride(name = "partnerC", column = @Column(name = "partner_c", columnDefinition = "TINYINT"))
    })
    private ChemistryCacheId cacheId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "requester_d", referencedColumnName = "requester_d", insertable = false, updatable = false),
            @JoinColumn(name = "requester_i", referencedColumnName = "requester_i", insertable = false, updatable = false),
            @JoinColumn(name = "requester_s", referencedColumnName = "requester_s", insertable = false, updatable = false),
            @JoinColumn(name = "requester_c", referencedColumnName = "requester_c", insertable = false, updatable = false),
            @JoinColumn(name = "partner_d", referencedColumnName = "partner_d", insertable = false, updatable = false),
            @JoinColumn(name = "partner_i", referencedColumnName = "partner_i", insertable = false, updatable = false),
            @JoinColumn(name = "partner_s", referencedColumnName = "partner_s", insertable = false, updatable = false),
            @JoinColumn(name = "partner_c", referencedColumnName = "partner_c", insertable = false, updatable = false)
    })
    private ChemistryCache chemistryCache;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // chemistry_cache 락 이후 INSERT. status=GENERATING으로 시작
    public static ChemistryReport create(User requester, User partner, TestType testType, ChemistryCacheId cacheId) {
        ChemistryReport cr = new ChemistryReport();
        cr.requester = requester;
        cr.partner = partner;
        cr.testType = testType;
        cr.status = ChemistryReportStatus.GENERATING;
        cr.cacheId = cacheId;
        cr.createdAt = LocalDateTime.now();
        return cr;
    }

    // LLM 보고서 생성 완료 후 호출. cacheId 세팅으로 chemistry_cache FK 연
    public void complete(ChemistryCacheId cacheId) {
        this.status = ChemistryReportStatus.READY;
        this.cacheId = cacheId;
    }

    // LLM 실패(@Retryable 소진) 또는 검사 결과 없음 시 호출
    public void fail() {
        this.status = ChemistryReportStatus.ERROR;
    }
}
