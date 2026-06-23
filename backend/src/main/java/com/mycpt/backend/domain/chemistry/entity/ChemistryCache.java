package com.mycpt.backend.domain.chemistry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * chemistry_cache 엔티티.
 *
 * disc_cache와 달리 사전 삽입 없음 — 순수 Lazy.
 * 미스 시 INSERT, 히트 시 refresh()로 UPDATE.
 */
@Entity
@Table(name = "chemistry_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChemistryCache {

    @EmbeddedId
    private ChemistryCacheId id;

    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 캐시 미스 시 신규 생성 (INSERT)
    public static ChemistryCache create(ChemistryCacheId id, String report, LocalDateTime now) {
        ChemistryCache cache = new ChemistryCache();
        cache.id = id;
        cache.report = report;
        cache.createdAt = now;
        return cache;
    }

    // 캐시 만료 시 갱신(UPDATE)
    public void refresh(String newReport, LocalDateTime now) {
        this.report = newReport;
        this.createdAt = now;
    }
}
