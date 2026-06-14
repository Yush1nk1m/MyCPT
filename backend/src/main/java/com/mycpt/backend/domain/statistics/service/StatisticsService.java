package com.mycpt.backend.domain.statistics.service;

import com.mycpt.backend.common.exception.BusinessException;import com.mycpt.backend.common.exception.ErrorCode;import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.statistics.dto.*;
import com.mycpt.backend.domain.statistics.repository.StatisticsRepository;
import com.mycpt.backend.domain.user.entity.User;import com.mycpt.backend.domain.user.enums.Gender;import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;import java.util.List;import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final UserRepository userRepository;

    // ── comparison ────────────────────────────────────────────────────────────

    /**
     * GET /statistics/comparison
     *
     * 1. 본인 최신 자기 평정 버킷 조회 (없으면 my.buckets = null)
     * 2. 본인 birthYear/gender 조회
     *  - 둘 중 하나라도 null이면 average = null 즉시 반환
     *  - gender = N(선택 안 함)이면 average = null (집계 대상 제외)
     * 3. 나이대(10년 단위) 계산 후 birthYear 범위로 DB 집계
     * 4. sampleCount = 0이면 average = null
     */
    public ComparisonResponse comparison(Long userId) {
        // 1. 본인 최신 SELF 버킷
        List<LatestBuckets> latest = statisticsRepository.findLatestBuckets(
                userId, RaterType.SELF, PageRequest.of(0, 1)
        );
        DiscBucketsDto myBuckets = latest.isEmpty() ? null
                : new DiscBucketsDto(
                    latest.get(0).dBucket(),
                    latest.get(0).iBucket(),
                    latest.get(0).sBucket(),
                    latest.get(0).cBucket()
                );

        // 2. 본인 birthYear/gender 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Integer birthYear = user.getBirthYear();
        Gender gender = user.getGender();

        // 생년 미입력, 성별 미입력, 또는 성별=N -> average null
        if (birthYear == null || gender == null || gender == Gender.N) {
            return new ComparisonResponse(
                    new ComparisonResponse.My(myBuckets),
                    null
            );
        }

        // 3. 나이대 계산 (10년 단위)
        int currentYear = LocalDateTime.now().getYear();
        int age = currentYear - birthYear;
        int decadeStart = (age / 10) * 10;
        int birthYearTo = currentYear - decadeStart;
        int birthYearFrom = currentYear - (decadeStart + 9);
        String ageGroupLabel = decadeStart + "대";

        // 4. 평균 집계
        Optional<BucketAverage> avgOpt = statisticsRepository.findAverageBuckets(
                RaterType.SELF, birthYearFrom, birthYearTo, gender
        );

        // sampleCount = 0이면 average null
        ComparisonResponse.Average average = avgOpt
                .filter(a -> a.sampleCount() > 0)
                .map(a -> new ComparisonResponse.Average(
                        ageGroupLabel,
                        gender.name(),
                        new DiscAverageDto(a.dBucket(), a.iBucket(), a.sBucket(), a.cBucket()),
                        a.sampleCount()
                ))
                .orElse(null);

        return new ComparisonResponse(
                new ComparisonResponse.My(myBuckets),
                average
        );
    }

    // ── trend ─────────────────────────────────────────────────────────────────

    /**
     * GET /statistics/trend
     *
     * days 기본 값 30. 결과 없으면 빈 배열 + count=0 반환
     * summary.average는 trend 포인트 전체의 산술 평균
     */
    public TrendResponse trend(Long userId, int days) {
        LocalDateTime from = LocalDateTime.now().minusDays(days);

        List<TrendPoint> points = statisticsRepository.findTrendBuckets(
                userId, RaterType.SELF, from
        );

        // trend 포인트 -> 응답 DTO 변환
        List<TrendResponse.TrendEntry> entries = points.stream()
                .map(p -> new TrendResponse.TrendEntry(
                        new DiscBucketsDto(p.dBucket(), p.iBucket(), p.sBucket(), p.cBucket()),
                        p.createdAt().toLocalDate().toString()
                ))
                .toList();

        // summary: 전체 평균 산출 (빈 배열이면 null)
        TrendResponse.Summary summary;
        if (points.isEmpty()) {
            summary = new TrendResponse.Summary(days + "days", null, 0);
        } else {
            double avgD = points.stream().mapToInt(TrendPoint::dBucket).average().orElse(0);
            double avgI = points.stream().mapToInt(TrendPoint::iBucket).average().orElse(0);
            double avgS = points.stream().mapToInt(TrendPoint::sBucket).average().orElse(0);
            double avgC = points.stream().mapToInt(TrendPoint::cBucket).average().orElse(0);
            summary = new TrendResponse.Summary(
                    days + "days",
                    new DiscAverageDto(avgD, avgI, avgS, avgC),
                    points.size()
            );
        }

        return new TrendResponse(summary, entries);
    }
}
