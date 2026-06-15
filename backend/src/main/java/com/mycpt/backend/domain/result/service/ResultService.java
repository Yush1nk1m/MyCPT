package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.result.dto.*;
import com.mycpt.backend.domain.result.entity.DiscTest;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.result.repository.DiscTestRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 결과 저장 서비스 (UC-01: 로그인하고 결과 저장)
 *
 * 흐름:
 *  1. ScoringService.normalize() - 원점수 재검증 + 버킷 산출
 *     (클라이언트 원점수를 신뢰하지 않음. sessionStorage 변조 방어)
 *  2. Test.createForSelf() -> testRepository.save()
 *  3. DiscTest.create()  -> discTestRepository.save()
 *  4. testId 반환 -> 컨트롤러가 201 { resultId } 로 응답
 *
 * disc_cache FK 보장:
 *  81개 행이 schema.sql 시드 스크립트로 사전 삽입됨
 *  정상 버킷(1~3)이면 FK 위반 없음
 */
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ScoringService scoringService;
    private final CacheService cacheService;
    private final DiscTestRepository discTestRepository;
    private final UserRepository userRepository;

    // ── 저장 ──────────────────────────────────────────────────────────────────

    /**
     * 자기 평정 결과 저장
     *
     * @param userId  인증된 회원 ID (@AuthenticationPrincipal 에서 추출)
     * @param request 클라이언트 sessionStorage에서 꺼낸 원점수
     * @return tests.id (resultId) - 프론트가 결과 상세 페이지로 이동 시 사용
     */
    @Transactional
    public Long save(Long userId, ScoreRequest request) {
        ScoringService.Buckets buckets = scoringService.normalize(request);
        ScoreRequest.Scores s = request.scores();

        User user = userRepository.getReferenceById(userId);
        // DiscTest.createForSelf()가 tests + disc_tests 두 테이블에 저장될 단일 엔티티 생성
        DiscTest discTest = DiscTest.createForSelf(
                user,
                s.d(), s.i(), s.s(), s.c(),
                buckets.d(), buckets.i(), buckets.s(), buckets.c()
        );
        discTestRepository.save(discTest);

        return discTest.getId();
    }

    // ── 조회 ──────────────────────────────────────────────────────────────────

    /**
     * 결과 이력 목록 조회 (GET /results)
     * <p>
     * 커서 기반 페이지네이션: size + 1개 조회 후 hasNext 판단
     * raterType == null이면 SELF/OTHER 전체 반환
     *
     * @param cursor 마지막으로 받은 resultId. null이면 최신순
     * @param size   페이지 크기 (기본 값은 컨트롤러에서 처리)
     */
    public ResultListResponse list(Long userId, RaterType raterType, Long cursor, int size) {
        List<DiscTest> rows = discTestRepository.findByUserIdWithCursor(
                userId, raterType, cursor, PageRequest.of(0, size + 1)
        );

        boolean hasNext = rows.size() > size;
        List<DiscTest> page = hasNext ? rows.subList(0, size) : rows;

        List<ResultSummaryResponse> results = page.stream()
                .map(dt -> new ResultSummaryResponse(
                        dt.getId(),
                        dt.getRaterType(),
                        dt.getLabel(),
                        new DiscBuckets(dt.getDBucket(), dt.getIBucket(), dt.getSBucket(), dt.getCBucket()),
                        dt.getCreatedAt()
                ))
                .toList();

        Long nextCursor = hasNext ? page.getLast().getId() : null;

        return new ResultListResponse(results, nextCursor, hasNext);
    }

    /**
     * 결과 상세 조회 (GET /results/{id})
     *
     * 본인 소유 검증: userId가 다르면 403
     * report는 CacheService 경유 - 버킷 값으로 disc_cache PK 룩업
     */
    public ResultDetailResponse detail(Long userId, Long testId) {
        DiscTest dt = discTestRepository.findByTestIdWithDetail(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 결과입니다."));

        if (!dt.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        ScoringService.Buckets buckets = new ScoringService.Buckets(
                dt.getDBucket(), dt.getIBucket(), dt.getSBucket(), dt.getCBucket()
        );
        String report = cacheService.getReport(buckets);

        return new ResultDetailResponse(
                dt.getId(),
                dt.getRaterType(),
                dt.getLabel(),
                new DiscScores(dt.getDScore(), dt.getIScore(), dt.getSScore(), dt.getCScore()),
                new DiscBuckets(dt.getDBucket(), dt.getIBucket(), dt.getSBucket(), dt.getCBucket()),
                report,
                dt.getCreatedAt()
        );
    }
}
