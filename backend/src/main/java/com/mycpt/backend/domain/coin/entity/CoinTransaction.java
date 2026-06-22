package com.mycpt.backend.domain.coin.entity;

import com.mycpt.backend.domain.coin.enums.CoinReason;
import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 코인 충전/차감 이력 (coin_transactions 테이블)
 * 이상 감지 및 CS 대응 용도. 행 삭제 없이 계속 INSERT만 발생.
 */
@Entity
@Table(name = "coin_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoinTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 양수: 충전 / 음수: 차감
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoinReason reason;

    // 트랜잭션 후 잔액 (이상 감지 용도)
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int balanceAfter;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private CoinTransaction(User user, int amount, CoinReason reason, int balanceAfter) {
        this.user = user;
        this.amount = amount;
        this.reason = reason;
        this.balanceAfter = balanceAfter;
        this.createdAt = LocalDateTime.now();
    }

    public static CoinTransaction create(User user, int amount, CoinReason reason, int balanceAfter) {
        return new CoinTransaction(user, amount, reason, balanceAfter);
    }
}
