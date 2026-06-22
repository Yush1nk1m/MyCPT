package com.mycpt.backend.domain.coin.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.coin.dto.CoinBalanceResponse;
import com.mycpt.backend.domain.coin.dto.CoinHistoryResponse;
import com.mycpt.backend.domain.coin.entity.CoinTransaction;
import com.mycpt.backend.domain.coin.enums.CoinReason;
import com.mycpt.backend.domain.coin.repository.CoinTransactionRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoinService 단위 테스트")
class CoinServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CoinTransactionRepository coinTransactionRepository;

    private CoinService sut() {
        return new CoinService(userRepository, coinTransactionRepository);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private User stubUser(int coins, LocalDateTime nextCoinAt) {
        User user = User.create("kakao-1", "유신", null);
        setId(user, 1L);
        // coins=3, nextCoinAt=null이 기본값이므로 테스트 시나리오에 맞게 리플렉션으로 조정
        setField(user, "coins", coins);
        setField(user, "nextCoinAt", nextCoinAt);
        return user;
    }

    private CoinTransaction stubTransaction(User user, Long id, int amount) {
        CoinTransaction tx = CoinTransaction.create(user, amount, CoinReason.CHEMISTRY_REPORT, 2);
        setId(tx, id);
        return tx;
    }

    // ── recordSignupBonus() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("recordSignupBonus()")
    class RecordSignupBonus {

        @Test
        @DisplayName("[UT-CoinSvc-가입보너스-성공]")
        void 가입보너스_성공() {
            // given
            User user = stubUser(3, null);

            // when
            sut().recordSignupBonus(user);

            // then
            then(coinTransactionRepository).should(times(1)).save(argThat(tx ->
                    tx.getAmount() == 3
                            && tx.getReason() == CoinReason.SIGNUP
                            && tx.getBalanceAfter() == 3
            ));
        }
    }

    // ── getBalance() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getBalance()")
    class GetBalance {

        @Test
        @DisplayName("[UT-CoinSvc-잔액조회-충전없음]")
        void 잔액조회_충전없음() {
            // given: nextCoinAt이 미래 시각
            User user = stubUser(1, LocalDateTime.now().plusHours(5));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            CoinBalanceResponse response = sut().getBalance(1L);

            // then: 충전 미발생
            assertThat(response.coins()).isEqualTo(1);
            then(coinTransactionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("[UT-CoinSvc-잔액조회-충전대기없음]")
        void 잔액조회_충전대기없음() {
            // given: nextCoinAt = null (만충 또는 대기 없음)
            User user = stubUser(3, null);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            CoinBalanceResponse response = sut().getBalance(1L);

            // then
            assertThat(response.coins()).isEqualTo(3);
            assertThat(response.nextCoinAt()).isNull();
            then(coinTransactionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("[UT-CoinSvc-잔액조회-온디맨드충전-1회]")
        void 잔액조회_온디맨드충전_1회() {
            // given: nextCoinAt이 1일 전 도래, coins=1
            LocalDateTime nextCoinAt = LocalDateTime.now().minusDays(1);
            User user = stubUser(1, nextCoinAt);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            CoinBalanceResponse response = sut().getBalance(1L);

            // then: coins=2로 충전, nextCoinAt이 1일 뒤로 이동(시분초 보존)
            assertThat(response.coins()).isEqualTo(2);
            assertThat(response.nextCoinAt()).isEqualTo(nextCoinAt.plusDays(1));
            then(coinTransactionRepository).should(times(1)).save(argThat(tx ->
                    tx.getAmount() == 1 && tx.getReason() == CoinReason.RECHARGE
            ));
        }

        @Test
        @DisplayName("[UT-CoinSvc-잔액조회-온디맨드충전-캡적용]")
        void 잔액조회_온디맨드충전_캡적용() {
            // given: nextCoinAt이 5일 전 도래, coins=1 -> chargeable=5지만 캡(3-1=2)
            LocalDateTime nextCoinAt = LocalDateTime.now().minusDays(5);
            User user = stubUser(1, nextCoinAt);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            CoinBalanceResponse response = sut().getBalance(1L);

            // then: coins=3(캡), nextCoinAt=null
            assertThat(response.coins()).isEqualTo(3);
            assertThat(response.nextCoinAt()).isNull();
            then(coinTransactionRepository).should(times(1)).save(argThat(tx ->
                    tx.getAmount() == 2 && tx.getReason() == CoinReason.RECHARGE
            ));
        }
    }

    // ── deduct() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deduct()")
    class Deduct {

        @Test
        @DisplayName("[UT-CoinSvc-차감-3미만최초설정]")
        void 차감_3미만최초설정() {
            // given: coins=3 -> 차감 후 coins=2 (nextCoinAt 미세팅 상태)
            User user = stubUser(3, null);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            sut().deduct(1L, CoinReason.CHEMISTRY_REPORT);

            // then: nextCoinAt이 null에서 now+24h 근방으로 세팅
            assertThat(user.getCoins()).isEqualTo(2);
            assertThat(user.getNextCoinAt()).isNotNull();
            assertThat(user.getNextCoinAt()).isAfter(LocalDateTime.now().plusHours(24).minusSeconds(10));
            then(coinTransactionRepository).should(times(1)).save(argThat(tx ->
                    tx.getAmount() == -1 && tx.getReason() == CoinReason.CHEMISTRY_REPORT
            ));
        }

        @Test
        @DisplayName("[UT-CoinSvc-차감-타이머기설정]")
        void 차감_타이머기설정() {
            // given: coins=2, nextCoinAt 이미 세팅됨
            LocalDateTime existingNextCoinAt = LocalDateTime.now().plusHours(10);
            User user = stubUser(2, existingNextCoinAt);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            sut().deduct(1L, CoinReason.CHEMISTRY_REPORT);

            // then: 기존 nextCoinAt 값 변경 없음
            assertThat(user.getCoins()).isEqualTo(1);
            assertThat(user.getNextCoinAt()).isEqualTo(existingNextCoinAt);
        }

        @Test
        @DisplayName("[UT-CoinSvc-차감-3이상설정]")
        void 차감_3이상설정() {
            // given: coins=4(이벤트 보유) -> 차감 후 3 (여전히 3 이상)
            User user = stubUser(4, null);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            sut().deduct(1L, CoinReason.CHEMISTRY_REPORT);

            // then: 3 이상이므로 nextCoinAt 세팅 안 됨
            assertThat(user.getCoins()).isEqualTo(3);
            assertThat(user.getNextCoinAt()).isNull();
        }

        @Test
        @DisplayName("[UT-CoinSvc-차감-잔액부족]")
        void 차감_잔액부족() {
            // given: coins=0
            User user = stubUser(0, LocalDateTime.now().plusHours(5));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            assertThatThrownBy(() -> sut().deduct(1L, CoinReason.CHEMISTRY_REPORT))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INSUFFICIENT_COINS);

            then(coinTransactionRepository).should(never()).save(any());
        }
    }

    // ── getHistory() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getHistory()")
    class GetHistory {

        @Test
        @DisplayName("[UT-CoinSvc-이력조회-성공]")
        void 이력조회_성공() {
            // given: 이력 5건 중 size=3 요청 -> 4건 조회(size+1)되어 hasNext 판단
            User user = stubUser(2, null);
            List<CoinTransaction> rows = List.of(
                    stubTransaction(user, 5L, -1),
                    stubTransaction(user, 4L, 1),
                    stubTransaction(user, 3L, -1),
                    stubTransaction(user, 2L, 1)
            );
            given(coinTransactionRepository.findHistory(eq(1L), isNull(), any(PageRequest.class)))
                    .willReturn(rows);

            // when
            CoinHistoryResponse response = sut().getHistory(1L, null, 3);

            // then: 3건 반환, hasNext=true, nextCursor=4번째 id(2L)
            assertThat(response.history()).hasSize(3);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo(3L);
        }

        @Test
        @DisplayName("[UT-CoinSvc-이력조회-마지막페이지]")
        void 이력조회_마지막페이지() {
            // given: 남은 이력이 size보다 적음
            User user = stubUser(2, null);
            List<CoinTransaction> rows = List.of(
                    stubTransaction(user, 2L, -1),
                    stubTransaction(user, 1L, 1)
            );
            given(coinTransactionRepository.findHistory(eq(1L), isNull(), any(PageRequest.class)))
                    .willReturn(rows);

            // when
            CoinHistoryResponse response = sut().getHistory(1L, null, 3);

            // then: hasNext=false, nextCursor=null
            assertThat(response.history()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }
    }
}