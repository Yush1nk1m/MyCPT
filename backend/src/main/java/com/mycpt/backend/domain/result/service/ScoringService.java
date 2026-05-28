package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.domain.result.dto.ScoreRequest;
import com.mycpt.backend.global.exception.InvalidScoreException;
import org.springframework.stereotype.Service;

/**
 * DISC 원점수 검증 및 버킷 정규화 서비스
 *
 * 책임:
 *   1. 개별 원점수 범위 검증 (-24 ~ +48)
 *   2. D + I + S + C 합계 검증 (= 24)
 *   3. 원점수 → 버킷값(1~9) 변환
 *
 * 버킷 정규화 공식:
 *   원점수 스팬: -24 ~ +48 (총 72)
 *   단계 수: 9 → 단계당 8점
 *   bucket = floor((score + 24) / 8) + 1, 상한 9 적용
 *
 * | 버킷 | 원점수 범위 | 의미      |
 * |------|-------------|-----------|
 * |  1   | -24 ~ -17   | 최하      |
 * |  2   | -16 ~  -9   | 매우 낮음 |
 * |  3   |  -8 ~  -1   | 낮음      |
 * |  4   |   0 ~  +7   | 보통      |
 * |  5   |  +8 ~ +15   | 보통 이상 |
 * |  6   | +16 ~ +23   | 높음      |
 * |  7   | +24 ~ +31   | 매우 높음 |
 * |  8   | +32 ~ +39   | 탁월      |
 * |  9   | +40 ~ +48   | 최상      |
 */
@Service
public class ScoringService {

    private static final int MIN_SCORE = -24;
    private static final int MAX_SCORE = 48;
    private static final int EXPECTED_SUM = 24;

    /**
     * 버킷 정규화 결과를 담는 레코드
     * CacheService.getReport()에 그대로 전달됨
     */
    public record Buckets(int d, int i, int s, int c) {}

    /**
     * 원점수를 검증하고 버킷 값으로 변환하여 반환
     * @param request POST /results/score 요청 바디
     * @return D/I/S/C 각각의 버킷 값 (1~9)
     * @throws InvalidScoreException 범위 위반 또는 합계 불일치 시
     */
    public Buckets normalize(ScoreRequest request) {
        ScoreRequest.Scores scores = request.scores();
        validate(scores);
        return new Buckets(
                toBucket(scores.d()),
                toBucket(scores.i()),
                toBucket(scores.s()),
                toBucket(scores.c()));
    }

    /**
     * 원점수 유효성 검증
     * 범위 초과 여부 검사 -> 합계 오류 검사
     */
    private void validate(ScoreRequest.Scores scores) {
        // 1. 개별 원점수 범위 검증
        int[] values = {scores.d(), scores.i(), scores.s(), scores.c()};
        for (int v : values) {
            if (v < MIN_SCORE || v > MAX_SCORE) {
                throw new InvalidScoreException(
                        "원점수는 %d 이상 %d 이하여야 합니다. 입력 값: %d".formatted(MIN_SCORE, MAX_SCORE, v));
            }
        }

        // 2. D + I + S + C 합계 검증
        int sum = scores.d() + scores.i() + scores.s() + scores.c();
        if (sum != EXPECTED_SUM) {
            throw new InvalidScoreException(
                    "D+I+S+C 합계는 %d여야 합니다. 입력 값: %d".formatted(EXPECTED_SUM, sum));
        }
    }

    /**
     * 원점수 하나를 버킷값(1~9)으로 변환
     *
     * package-private: 외부 노출 없이 ScoringServiceTest에서 직접 호출 가능
     *
     * 계산 예시:
     *   score = -24 → (-24 + 24) / 8 + 1 = 0 + 1 = 1
     *   score =   0 → (  0 + 24) / 8 + 1 = 3 + 1 = 4
     *   score =  48 → ( 48 + 24) / 8 + 1 = 9 + 1 = 10 → min(10, 9) = 9
     *
     * Math.min으로 상한 9를 적용하는 이유:
     *   score = 40: (40 + 24) / 8 + 1 = 8 + 1 = 9 (정상)
     *   score = 48: (48 + 24) / 8 + 1 = 9 + 1 = 10 → 9로 클램핑
     */
    int toBucket(int score) {
        return Math.min((score + 24) / 8 + 1, 9);
    }
}
