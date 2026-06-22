package com.mycpt.backend.domain.chemistry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * chemistry_cache 복합 PK: (requester 4축 + partner 4축)
 * A/B 순서 미정규화 — requester/partner 주어가 다른 보고서이므로 별도 캐시
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChemistryCacheId implements Serializable {

    @Column(name = "requester_d", nullable = false, columnDefinition = "TINYINT")
    private int requesterD;

    @Column(name = "requester_i", nullable = false, columnDefinition = "TINYINT")
    private int requesterI;

    @Column(name = "requester_s", nullable = false, columnDefinition = "TINYINT")
    private int requesterS;

    @Column(name = "requester_c", nullable = false, columnDefinition = "TINYINT")
    private int requesterC;

    @Column(name = "partner_d", nullable = false, columnDefinition = "TINYINT")
    private int partnerD;

    @Column(name = "partner_i", nullable = false, columnDefinition = "TINYINT")
    private int partnerI;

    @Column(name = "partner_s", nullable = false, columnDefinition = "TINYINT")
    private int partnerS;

    @Column(name = "partner_c", nullable = false, columnDefinition = "TINYINT")
    private int partnerC;

    public ChemistryCacheId(
            int requesterD, int requesterI, int requesterS, int requesterC,
            int partnerD, int partnerI, int partnerS, int partnerC
    ) {
        this.requesterD = requesterD;
        this.requesterI = requesterI;
        this.requesterS = requesterS;
        this.requesterC = requesterC;
        this.partnerD = partnerD;
        this.partnerI = partnerI;
        this.partnerS = partnerS;
        this.partnerC = partnerC;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChemistryCacheId that)) return false;
        return requesterD == that.requesterD && requesterI == that.requesterI
                && requesterS == that.requesterS && requesterC == that.requesterC
                && partnerD == that.partnerD && partnerI == that.partnerI
                && partnerS == that.partnerS && partnerC == that.partnerC;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                requesterD, requesterI, requesterS, requesterC,
                partnerD, partnerI, partnerS, partnerC
        );
    }
}
