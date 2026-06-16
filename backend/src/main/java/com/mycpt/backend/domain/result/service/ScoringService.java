package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.result.dto.DiscScoreRequest;
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
     * 3구간 경계 값
     * Low  :   -24 ~ -5    (구간 폭 20)
     * Mid  :   -4 ~ +11    (구간 폭 16, 데이터 밀집 중심부)
     * High :   +12 ~ +48   (구간 폭 37, 확실한 주성향)
     */
    private static final int LOW_MAX = -5;
    private static final int MID_MAX = 11;

    /**
     * 버킷 정규화 결과를 담는 레코드
     * CacheService.getReport()에 그대로 전달됨
     */
    public record Buckets(int d, int i, int s, int c) {}

    /**
     * 원점수를 검증하고 버킷 값으로 변환하여 반환
     * @param request POST /results/score 요청 바디
     * @return D/I/S/C 각각의 버킷 값 (1~9)
     * @throws BusinessException (INVALID_SCORE) 범위 위반 또는 합계 불일치 시
     */
    public Buckets normalize(DiscScoreRequest request) {
        DiscScoreRequest.Scores scores = request.scores();
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
    private void validate(DiscScoreRequest.Scores scores) {
        // 1. 개별 원점수 범위 검증
        int[] values = {scores.d(), scores.i(), scores.s(), scores.c()};
        for (int v : values) {
            if (v < MIN_SCORE || v > MAX_SCORE) {
                throw new BusinessException(ErrorCode.INVALID_SCORE,
                        "원점수는 %d 이상 %d 이하여야 합니다. 입력 값: %d".formatted(MIN_SCORE, MAX_SCORE, v));
            }
        }

        // 2. D + I + S + C 합계 검증
        int sum = scores.d() + scores.i() + scores.s() + scores.c();
        if (sum != EXPECTED_SUM) {
            throw new BusinessException(ErrorCode.INVALID_SCORE,
                    "D+I+S+C 합계는 %d여야 합니다. 입력 값: %d".formatted(EXPECTED_SUM, sum));
        }
    }

    /**
     * 원점수 → 3구간 버킷값 변환
     *
     * | 버킷 | 원점수 범위   | 의미                          |
     * |------|---------------|-------------------------------|
     * |  1   | -24 ~  -5     | Low  — 해당 성향 기피/거부    |
     * |  2   |  -4 ~ +11     | Mid  — 상황적 유연 발현       |
     * |  3   | +12 ~ +48     | High — 확실한 주성향          |
     *
     * package-private: ScoringServiceTest에서 직접 호출
     */
    int toBucket(int score) {
        if (score <= LOW_MAX) return 1;
        if (score <= MID_MAX) return 2;
        return 3;
    }
}
