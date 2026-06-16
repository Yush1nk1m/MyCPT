package com.mycpt.backend.domain.statistics.service;

import com.mycpt.backend.domain.statistics.dto.*;
import com.mycpt.backend.domain.statistics.repository.StatisticsRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.enums.Gender;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService 단위 테스트")
class StatisticsServiceTest {

    @Mock
    private StatisticsRepository statisticsRepository;
    @Mock
    private UserRepository userRepository;

    private StatisticsService sut() {
        return new StatisticsService(statisticsRepository, userRepository);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    // bitrhYear/gender 모두 입력된 정상 사용자
    private User stubUserFull(Gender gender) {
        User user = User.create("kakao-1", "닉네임", null);
        user.updateProfile("닉네임", 1995, gender);
        return user;
    }

    // birthYear/gender 미입력 사용자
    private User stubUserEmpty() {
        return User.create("kakao-1", "닉네임", null);
    }

    // LatestBuckets 스텁
    private LatestBuckets latestBuckets(int d, int i, int s, int c) {
        return new LatestBuckets(d, i, s, c);
    }

    // BucketAverage 스텁 (sampleCount > 0)
    private BucketAverage bucketAverage(long count) {
        return new BucketAverage(2.0, 2.0, 2.0, 2.0, count);
    }

    // TrendPoint 스텁
    private TrendPoint trendPoint(int d, int i, int s, int c) {
        return new TrendPoint(d, i, s, c, LocalDateTime.now());
    }

    // ── comparison() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("comparison()")
    class Comparison {

        @Test
        @DisplayName("[UT-StatisticsSvc-비교조회-검사없음]")
        void 비교조회_검사없음() {
            // given: SELF 검사 이력 없음 -> AVERAGE 집계는 계속 진행
            given(statisticsRepository.findLatestBuckets(anyLong(), any(), any(Pageable.class)))
                    .willReturn(List.of());
            given(userRepository.findById(anyLong()))
                    .willReturn(Optional.of(stubUserFull(Gender.M)));
            given(statisticsRepository.findAverageBuckets(any(), anyInt(), anyInt(), any()))
                    .willReturn(Optional.of(bucketAverage(10)));

            // when
            ComparisonResponse response = sut().comparison(1L);

            // then: my.buckets = null, average는 반환
            assertThat(response.my().buckets()).isNull();
            assertThat(response.average()).isNotNull();
        }

        @Test
        @DisplayName("[UT-StatisticsSvc-비교조회-생년미입력]")
        void 비교조회_생년미입력() {
            // given
            given(statisticsRepository.findLatestBuckets(anyLong(), any(), any(Pageable.class)))
                    .willReturn(List.of(latestBuckets(3, 2, 2, 1)));
            given(userRepository.findById(anyLong()))
                    .willReturn(Optional.of(stubUserEmpty()));  // birthYear = null

            // when
            ComparisonResponse response = sut().comparison(1L);

            // then
            assertThat(response.average()).isNull();
        }

        @Test
        @DisplayName("[UT-StatisticsSvc-비교조회-성별미입력]")
        void 비교조회_성별미입력() {
            // given: birthYear 있음, gender = null
            User user = User.create("kakao-1", "닉네임", null);
            user.updateProfile("닉네임", 1995, null);
            given(statisticsRepository.findLatestBuckets(anyLong(), any(), any(Pageable.class)))
                    .willReturn(List.of(latestBuckets(3, 2, 2, 1)));
            given(userRepository.findById(anyLong()))
                    .willReturn(Optional.of(user));

            // when
            ComparisonResponse response = sut().comparison(1L);

            // then
            assertThat(response.average()).isNull();
        }

        @Test
        @DisplayName("[UT-StatisticsSvc-비교조회-성별N]")
        void 비교조회_성별N() {
            // given
            given(statisticsRepository.findLatestBuckets(anyLong(), any(), any(Pageable.class)))
                    .willReturn(List.of(latestBuckets(3, 2, 2, 1)));
            given(userRepository.findById(anyLong()))
                    .willReturn(Optional.of(stubUserFull(Gender.N)));

            // when
            ComparisonResponse response = sut().comparison(1L);

            // then
            assertThat(response.average()).isNull();
        }

        @Test
        @DisplayName("[UT-StatisticsSvc-비교조회-샘플없음]")
        void 비교조회_샘플없음() {
            // given: 집계 결과 sampleCount = 0
            given(statisticsRepository.findLatestBuckets(anyLong(), any(), any(Pageable.class)))
                    .willReturn(List.of(latestBuckets(3, 2, 2, 1)));
            given(userRepository.findById(anyLong()))
                    .willReturn(Optional.of(stubUserFull(Gender.M)));
            given(statisticsRepository.findAverageBuckets(any(), anyInt(), anyInt(), any()))
                    .willReturn(Optional.of(bucketAverage(0)));

            // when
            ComparisonResponse response = sut().comparison(1L);

            // then
            assertThat(response.average()).isNull();
        }

        @Test
        @DisplayName("[UT-StatisticsSvc-비교조회-성공]")
        void 비교조회_성공() {
            // given
            given(statisticsRepository.findLatestBuckets(anyLong(), any(), any(Pageable.class)))
                    .willReturn(List.of(latestBuckets(3, 2, 2, 1)));
            given(userRepository.findById(anyLong()))
                    .willReturn(Optional.of(stubUserFull(Gender.M)));
            given(statisticsRepository.findAverageBuckets(any(), anyInt(), anyInt(), any()))
                    .willReturn(Optional.of(bucketAverage(15)));

            // when
            ComparisonResponse response = sut().comparison(1L);

            // then
            assertThat(response.my().buckets()).isNotNull();
            assertThat(response.average()).isNotNull();
            assertThat(response.average().sampleCount()).isEqualTo(15);
            assertThat(response.average().ageGroup()).endsWith("대");
        }
    }

    // ── trend() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("trend()")
    class Trend {

        @Test
        @DisplayName("[UT-StatisticsSvc-추이조회-결과없음]")
        void 추이조회_결과없음() {
            // given
            given(statisticsRepository.findTrendBuckets(anyLong(), any(), any(LocalDateTime.class)))
                    .willReturn(List.of());

            // when
            TrendResponse response = sut().trend(1L, 30);

            // then
            assertThat(response.trend()).isEmpty();
            assertThat(response.summary().count()).isZero();
            assertThat(response.summary().average()).isNull();
        }

        @Test
        @DisplayName("[UT-StatisticsSvc-추이조회-성공]")
        void 추이조회_성공() {
            // given: 버킷 값 합이 각각 명확한 포인트 2개
            // D: (1+3)/2=2.0, I: (2+2)/2=2.0, S: (3+1)/2=2.0, C: (2+2)/2=2.0
            given(statisticsRepository.findTrendBuckets(anyLong(), any(), any(LocalDateTime.class)))
                    .willReturn(List.of(
                            trendPoint(1, 2, 3, 2),
                            trendPoint(3, 2, 1, 2)
                    ));

            // when
            TrendResponse response = sut().trend(1L, 30);

            // then
            assertThat(response.trend()).hasSize(2);
            assertThat(response.summary().count()).isEqualTo(2);
            assertThat(response.summary().average().d()).isEqualTo(2.0);
            assertThat(response.summary().average().i()).isEqualTo(2.0);
            assertThat(response.summary().average().s()).isEqualTo(2.0);
            assertThat(response.summary().average().c()).isEqualTo(2.0);
        }
    }
}