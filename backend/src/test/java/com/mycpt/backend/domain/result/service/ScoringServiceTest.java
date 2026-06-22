package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.result.dto.DiscScoreRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * ScoringService 단위 테스트.
 *
 * 테스트 대상:
 *   - toBucket(): 버킷 정규화 공식 및 경계값
 *   - normalize(): 정상 흐름 및 검증 실패 케이스
 *
 * ScoringService는 외부 의존성이 없으므로 직접 인스턴스화하여 사용.
 * @ExtendWith(MockitoExtension.class) 는 테스트 ID 체계의 UT 컨벤션 유지 목적.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScoringService 단위 테스트")
class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService();

    // ── toBucket() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toBucket() - 버킷 정규화 경계 값")
    class ToBucket {

        /**
         * 3구간 경계값 전수 검증
         *
         * | 버킷 | 범위        | 경계 케이스          |
         * |------|-------------|----------------------|
         * |  1   | -24 ~  -5   | 하한 -24, 상한 -5    |
         * |  2   |  -4 ~ +11   | 하한  -4, 상한 +11   |
         * |  3   | +12 ~ +48   | 하한 +12, 상한 +48   |
         */
        @ParameterizedTest(name = "원점수 {0} -> 버킷 {1}")
        @CsvSource({
                "-24, 1",   // 버킷 1 하한 (전체 최솟값)
                " -5, 1",   // 버킷 1 상한
                " -4, 2",   // 버킷 2 하한 (버킷 1→2 전환점)
                " 11, 2",   // 버킷 2 상한
                " 12, 3",   // 버킷 3 하한 (버킷 2→3 전환점)
                " 48, 3",   // 버킷 3 상한 (전체 최댓값)
        })
        void 버킷정규화경계값분석(int score, int expectedBucket) {
            assertThat(scoringService.toBucket(score)).isEqualTo(expectedBucket);
        }
    }

    // ── normalize() 정상 흐름 ────────────────────────────────────────────────

    @Nested
    @DisplayName("normalize() - 정상 입력")
    class Normalize {

        @Test
        @DisplayName("[UT-ScoringSvc-버킷정규화-성공]")
        void 버킷정규화_성공() {
            // given: D=32(High), I=10(Mid), S=-4(Mid), C=-14(Low)
            DiscScoreRequest request = request(32, 10, -4, -14);

            // when
            ScoringService.Buckets buckets = scoringService.normalize(request);

            // then
            assertThat(buckets.d()).isEqualTo(3);
            assertThat(buckets.i()).isEqualTo(2);
            assertThat(buckets.s()).isEqualTo(2);
            assertThat(buckets.c()).isEqualTo(1);
        }

        @Test
        @DisplayName("[UT-ScoringSvc-버킷정규화-최솟값최댓값혼합]")
        void 버킷정규화_최솟값최댓값혼합() {
            // given: D=-24(Low 하한), C=48(High 상한), I+S=0 합계 맞춤
            DiscScoreRequest request = request(-24, 0, 0, 48);

            // when
            ScoringService.Buckets buckets = scoringService.normalize(request);

            // then
            assertThat(buckets.d()).isEqualTo(1);
            assertThat(buckets.i()).isEqualTo(2);
            assertThat(buckets.s()).isEqualTo(2);
            assertThat(buckets.c()).isEqualTo(3);
        }
    }

    // ── validate() 예외 케이스 ───────────────────────────────────────────────

    @Nested
    @DisplayName("normalize() - 검증 실패")
    class Validate {

        @Test
        @DisplayName("[UT-ScoringSvc-합계검증-실패]")
        void 합계검증_실패() {
            // given
            DiscScoreRequest request = request(10, 10, 10, -5);

            // when
            assertThatThrownBy(() -> scoringService.normalize(request))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_SCORE);
                        assertThat(be.getMessage()).contains("합계는 24여야 합니다.");
                    });
        }

        @Test
        @DisplayName("[UT-ScoringSvc-범위초과-상한]")
        void 범위초과_상한() {
            // given
            DiscScoreRequest request = request(49, 0, 0, 0);

            // when
            assertThatThrownBy(() -> scoringService.normalize(request))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_SCORE);
                        assertThat(be.getMessage()).contains("49");
                    });
        }

        @Test
        @DisplayName("[UT-ScoringSvc-범위초과-하한")
        void 범위초과_하한() {
            // given
            DiscScoreRequest request = request(0, -25, 0, 0);

            // when
            assertThatThrownBy(() -> scoringService.normalize(request))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_SCORE);
                        assertThat(be.getMessage()).contains("-25");
                    });
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    /**
     * 테스트용 ScoreReqeust 생성 헬퍼
     * - 코드 반복 감소
     * - 의도 명확화
     */
    private DiscScoreRequest request(int d, int i, int s, int c) {
        return new DiscScoreRequest(new DiscScoreRequest.Scores(d, i, s, c));
    }
}