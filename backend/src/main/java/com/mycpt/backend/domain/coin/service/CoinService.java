package com.mycpt.backend.domain.coin.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.coin.dto.CoinBalanceResponse;
import com.mycpt.backend.domain.coin.dto.CoinHistoryResponse;
import com.mycpt.backend.domain.coin.dto.CoinTransactionResponse;
import com.mycpt.backend.domain.coin.entity.CoinTransaction;
import com.mycpt.backend.domain.coin.enums.CoinReason;
import com.mycpt.backend.domain.coin.repository.CoinTransactionRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoinService {

    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;

    /**
     * 가입 시 초기 코인 3개 지급 — coin_transactions에 SIGNUP 기록만 남김
     * User.coins는 User.create()에서 이미 3으로 세팅되어 있으므로 잔액 변경 없음
     * CustomOAuth2UserService.findOrCreateUser()의 save() 직후 호출
     */
    @Transactional
    public void recordSignupBonus(User user) {
        coinTransactionRepository.save(
                CoinTransaction.create(user, 3, CoinReason.SIGNUP, user.getCoins())
        );
    }

    /**
     * 코인 잔액 조회. 충전 조건 충족 시 온디맨드 충전 후 반환.
     * GET /coins
     */
    @Transactional
    public CoinBalanceResponse getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        chargeIfDue(user);

        return new CoinBalanceResponse(user.getCoins(), user.getNextCoinAt());
    }

    /**
     * 코인 1개 차감 (케미 보고서 발행 시 호출)
     * 차감 전 충전 조건을 먼저 반영한 뒤 잔액 검증
     */
    @Transactional
    public void deduct(Long userId, CoinReason reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        chargeIfDue(user);

        if (user.getCoins() <= 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_COINS);
        }

        user.deductCoin(LocalDateTime.now());

        coinTransactionRepository.save(
                CoinTransaction.create(user, -1, reason, user.getCoins())
        );
    }

    /**
     * 코인 이력 조회 (커서 기반 페이지네이션)
     * GET /coins/history
     */
    public CoinHistoryResponse getHistory(Long userId, Long cursor, int size) {
        // size + 1개를 조회해서 hasNext 판단
        List<CoinTransaction> rows = coinTransactionRepository.findHistory(
                userId, cursor, PageRequest.of(0, size + 1)
        );

        boolean hasNext = rows.size() > size;
        List<CoinTransaction> page = hasNext ? rows.subList(0, size) : rows;

        List<CoinTransactionResponse> items = page.stream()
                .map(CoinTransactionResponse::from)
                .toList();

        Long nextCursor = hasNext ? page.getLast().getId() : null;

        return new CoinHistoryResponse(items, nextCursor, hasNext);
    }

    /**
     * 온디맨드 충전 — next_coin_at이 지난 만큼 충전 후 user 엔티티에 반영
     * 정책: next_coin_at의 시:분:초가 NOW까지 몇 번 도래했는지로 충전 개수 산출
     *       3개 캡, 만충 도달 시 next_coin_at = null
     */
    private void chargeIfDue(User user) {
        LocalDateTime nextCoinAt = user.getNextCoinAt();
        if (nextCoinAt == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(nextCoinAt)) {
            return;
        }

        // next_coin_at이 지난 일수만큼 도래 횟수 산출 (시:분:초는 보존되므로 날짜 차이로 계산)
        int chargeable = (int) Duration.between(nextCoinAt, now).toDays();

        int amount = Math.min(chargeable, 3 - user.getCoins());
        if (amount <= 0) {
            return;
        }

        user.increaseCoins(amount);

        coinTransactionRepository.save(
                CoinTransaction.create(user, amount, CoinReason.RECHARGE, user.getCoins())
        );
    }
}
