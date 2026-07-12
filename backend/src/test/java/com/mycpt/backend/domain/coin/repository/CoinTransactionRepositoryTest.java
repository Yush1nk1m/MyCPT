package com.mycpt.backend.domain.coin.repository;

import com.mycpt.backend.domain.coin.entity.CoinTransaction;
import com.mycpt.backend.domain.coin.enums.CoinReason;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.JpaTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CoinTransactionRepository 슬라이스 테스트")
class CoinTransactionRepositoryTest extends JpaTestSupport {

    @Autowired private CoinTransactionRepository coinTransactionRepository;
    @Autowired private UserRepository userRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.create("kakao-a", "A", null));
        userB = userRepository.save(User.create("kakao-b", "B", null));
    }

    @Nested
    @DisplayName("deleteByUserId()")
    class DeleteByUserId {

        @Test
        @DisplayName("[ST-CoinTxRepo-사용자별삭제-성공]")
        void 사용자별삭제_성공() {
            // given
            coinTransactionRepository.save(CoinTransaction.create(userA, 3, CoinReason.SIGNUP, 3));
            coinTransactionRepository.save(CoinTransaction.create(userA, -1, CoinReason.CHEMISTRY_REPORT, 2));
            coinTransactionRepository.save(CoinTransaction.create(userB, 3, CoinReason.SIGNUP, 3));

            // when
            coinTransactionRepository.deleteByUserId(userA.getId());

            // then
            assertThat(coinTransactionRepository.findAll())
                    .allMatch(tx -> tx.getUser().getId().equals(userB.getId()));
        }
    }

}