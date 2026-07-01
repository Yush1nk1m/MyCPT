package com.mycpt.backend.domain.chemistry.entity;

import com.mycpt.backend.domain.chemistry.enums.ChemistryCacheStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * chemistry_cache 테이블 엔티티.
 *
 * 6,561행(81×81) 사전 삽입 — disc_cache와 동일하게 UPDATE-only.
 *
 * status 컬럼이 락 라이프사이클을 담당 (chemistry_reports.status와 별개 관심사):
 *   NULL       → 미생성. SELECT FOR UPDATE 후 이 값을 발견한 스레드가 발행자.
 *   GENERATING → LLM 호출 중. 이 값을 발견한 스레드는 구독자 → Redis Pub/Sub 대기.
 *   READY      → 생성 완료. 이 값을 발견한 스레드는 즉시 report 반환.
 *
 * 만료 판단: status=READY && created_at < expireLine → 재생성 (발행자 경로 재진입)
 */
@Entity
@Table(name = "chemistry_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChemistryCache {

    @EmbeddedId
    private ChemistryCacheId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChemistryCacheStatus status;

    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // NULL -> GENERATING: 발행자로 확정된 직후 호출. 커밋으로 락 즉시 해제
    public void startGenerating() {
        this.status = ChemistryCacheStatus.GENERATING;
        this.updatedAt = LocalDateTime.now();
    }

    // GENERATING -> READY: LLM 완료 후 호출
    public void complete(String report, LocalDateTime now) {
        this.status = ChemistryCacheStatus.READY;
        this.report = report;
        this.createdAt = now;
        this.updatedAt = now;
    }

    // READY + 만료 -> 재생성 시작: 발행자 경로 재진입. startGenerating() 후 LLM 재호출
    public void refresh() {
        this.status = ChemistryCacheStatus.GENERATING;
        this.report = null;
        this.createdAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    // 배치가 GENERATING 상태를 재시도 대상으로 확정할 때 호출.
    // status/report는 건드리지 않고 updatedAt만 갱신 -> 다음 배치 주기(10분) 동안 재선택 방지
    // (배치 중복 실행에 대한 뮤텍스 역할까지 겸함)
    public void markRetryStartred() {
        this.updatedAt = LocalDateTime.now();
    }
}
