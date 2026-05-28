package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.domain.result.dto.ScoreRequest;
import com.mycpt.backend.global.exception.InvalidScoreException;
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
         * 9단계 버킷의 하한/상한 경계 값과 버킷 간 전환점을 모두 검증
         * 서비스 설계 문서(service-design.md) §3.5 버킷 테이블과 1:1 대응
         */
        @ParameterizedTest(name = "원점수 {0} -> 버킷 {1}")
        @CsvSource({
                "-24, 1",   // 버킷 1 하한 (전체 최솟값)
                "-17, 1",   // 버킷 1 상한
                "-16, 2",   // 버킷 2 하한 (버킷 1→2 전환점)
                " -9, 2",   // 버킷 2 상한
                " -8, 3",   // 버킷 3 하한
                " -1, 3",   // 버킷 3 상한
                "  0, 4",   // 버킷 4 하한
                "  7, 4",   // 버킷 4 상한
                "  8, 5",   // 버킷 5 하한
                " 15, 5",   // 버킷 5 상한
                " 16, 6",   // 버킷 6 하한
                " 23, 6",   // 버킷 6 상한
                " 24, 7",   // 버킷 7 하한
                " 31, 7",   // 버킷 7 상한
                " 32, 8",   // 버킷 8 하한
                " 39, 8",   // 버킷 8 상한
                " 40, 9",   // 버킷 9 하한
                " 48, 9",   // 버킷 9 상한 (전체 최댓값, 클램핑 적용)
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
        @DisplayName("[UT-ScoringService-버킷정규화-성공]")
        void 버킷정규화_성공() {
            // given
            ScoreRequest request = request(32, 10, -4, -14);

            // when
            ScoringService.Buckets buckets = scoringService.normalize(request);

            // then
            assertThat(buckets.d()).isEqualTo(8);
            assertThat(buckets.i()).isEqualTo(5);
            assertThat(buckets.s()).isEqualTo(3);
            assertThat(buckets.c()).isEqualTo(2);
        }

        @Test
        @DisplayName("[UT-ScoringService-버킷정규화-최솟값최댓값혼합]")
        void 버킷정규화_최솟값최댓값혼합() {
            // given
            ScoreRequest request = request(-24, 0, 0, 48);

            // when
            ScoringService.Buckets buckets = scoringService.normalize(request);

            // then
            assertThat(buckets.d()).isEqualTo(1);
            assertThat(buckets.i()).isEqualTo(4);
            assertThat(buckets.s()).isEqualTo(4);
            assertThat(buckets.c()).isEqualTo(9);
        }
    }

    // ── validate() 예외 케이스 ───────────────────────────────────────────────

    @Nested
    @DisplayName("normalize() - 검증 실패")
    class Validate {

        @Test
        @DisplayName("[UT-ScoringService-합계검증-실패]")
        void 합계검증_실패() {
            // given
            ScoreRequest request = request(10, 10, 10, -5);

            // when
            assertThatThrownBy(() -> scoringService.normalize(request))
                    // then
                    .isInstanceOf(InvalidScoreException.class)
                    .hasMessageContaining("합계는 24여야 합니다.");
        }

        @Test
        @DisplayName("[UT-ScoringService-범위초과-상한]")
        void 범위초과_상한() {
            // given
            ScoreRequest request = request(49, 0, 0, 0);

            // when
            assertThatThrownBy(() -> scoringService.normalize(request))
                    // then
                    .isInstanceOf(InvalidScoreException.class)
                    .hasMessageContaining("49");
        }

        @Test
        @DisplayName("[UT-ScoringService-범위초과-하한")
        void 범위초과_하한() {
            // given
            ScoreRequest request = request(0, -25, 0, 0);

            // when
            assertThatThrownBy(() -> scoringService.normalize(request))
                    // then
                    .isInstanceOf(InvalidScoreException.class)
                    .hasMessageContaining("-25");
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    /**
     * 테스트용 ScoreReqeust 생성 헬퍼
     * - 코드 반복 감소
     * - 의도 명확화
     */
    private ScoreRequest request(int d, int i, int s, int c) {
        return new ScoreRequest("DISC", new ScoreRequest.Scores(d, i, s, c));
    }
}