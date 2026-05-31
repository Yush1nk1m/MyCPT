package com.mycpt.backend.domain.result.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * disc_cache 복합 PK: (d, i, s, c) 버킷 값 조합
 *
 * JPA @EmbeddedId 요건:
 *  - @Embeddable 선언
 *  - Serializable 구현 (JPA 스펙 요구사항)
 *  - equals() / hashCode() 구현
 *      -> JPA 1차 캐시가 이 값으로 엔티티를 식별하므로 반드시 필요
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscCacheId implements Serializable {

    @Column(name = "d", nullable = false)
    private int d;

    @Column(name = "i", nullable = false)
    private int i;

    @Column(name = "s", nullable = false)
    private int s;

    @Column(name = "c", nullable = false)
    private int c;

    public DiscCacheId(int d, int i, int s, int c) {
        this.d = d;
        this.i = i;
        this.s = s;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DiscCacheId that)) return false;
        return d == that.d && i == that.i && s == that.s && c == that.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(d, i, s, c);
    }
}
