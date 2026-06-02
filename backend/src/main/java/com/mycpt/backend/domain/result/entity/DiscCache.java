package com.mycpt.backend.domain.result.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * disc_cache 테이블 엔티티
 *
 * 설계 원칙 (database-design.md):
 *  - 81개 행이 초기화 스크립트에 의해 사전 삽입됨 (report=NULL, created_at=NULL)
 *  - report NULL = 아직 LLM 보고서가 생성되지 않은 상태
 *  - 행 삭제 없이 UPDATE만으로 갱신
 *    이유: disc_results -> disc_cache 복합 FK가 있으므로 DELETE 시 FK 위반 발생.
 *         refresh()로 report + created_at만 교체
 *  - report에 이름 미포함: 렌더링 시점에 이름을 삽입하는 방식이므로
 *    동일 버킷을 공유하는 모든 사용자에게 같은 캐시 행을 재사용 가능
 */
@Entity
@Table(name = "disc_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscCache {

    @EmbeddedId
    private DiscCacheId id; // 복합 PK: (d, i, s, c)

    // Markdown 형식 보고서 전문. 이름 미포함
    // NULL = 사전 삽입 후 아직 LLM 보고서가 생성되지 않은 상태
    @Column(nullable = true, columnDefinition = "TEXT")
    private String report;

    // 캐시 생성/갱신 시각. 온디맨드 만료 판단 기준 (CacheService에서 비교)
    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;

    public DiscCache(DiscCacheId id, String report, LocalDateTime createdAt) {
        this.id = id;
        this.report = report;
        this.createdAt = createdAt;
    }

    /**
     * 캐시 갱신 - MISS 시 INSERT 대신 이미 존재하는 행을 UPDATE할 때 호출
     * report와 created_at만 교체하여 FK 참조 무결성 유지
     */
    public void refresh(String newReport, LocalDateTime now) {
        this.report = newReport;
        this.createdAt = now;
    }
}
