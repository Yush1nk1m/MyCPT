package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.result.dto.*;
import com.mycpt.backend.domain.result.entity.DiscResult;
import com.mycpt.backend.domain.result.entity.Test;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.result.repository.DiscResultRepository;
import com.mycpt.backend.domain.result.repository.TestRepository;
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
 *  3. DiscResult.create()  -> discResultRepository.save()
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
    private final TestRepository testRepository;
    private final DiscResultRepository discResultRepository;
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
        // 1. 원점수 재검증 + 버킷 산출. ScoringService가 범위 오류·합계 불일치 시 InvalidScoreException 던짐
        ScoringService.Buckets buckets = scoringService.normalize(request);
        ScoreRequest.Scores s = request.scores();

        // 2. tests INSERT - rater_type=SELF, label=null
        User user = userRepository.getReferenceById(userId);
        Test test = Test.createForSelf(user, request.testType());
        testRepository.save(test);

        // 3. disc_results INSERT
        DiscResult discResult = DiscResult.create(
                test,
                s.d(), s.i(), s.s(), s.c(),
                buckets.d(), buckets.i(), buckets.s(), buckets.c()
        );
        discResultRepository.save(discResult);

        return test.getId();
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
        // size + 1 조회로 다음 페이지 존재 여부 판단
        List<DiscResult> rows = discResultRepository.findByUserIdWithCursor(
                userId, raterType, cursor, PageRequest.of(0, size + 1)
        );

        boolean hasNext = rows.size() > size;
        List<DiscResult> page = hasNext ? rows.subList(0, size) : rows;

        List<ResultSummaryResponse> results = page.stream()
                .map(dr -> new ResultSummaryResponse(
                        dr.getTest().getId(),
                        dr.getTest().getRaterType(),
                        dr.getTest().getLabel(),
                        new DiscBuckets(dr.getDBucket(), dr.getIBucket(), dr.getSBucket(), dr.getCBucket()),
                        dr.getTest().getCreatedAt()
                ))
                .toList();

        Long nextCursor = hasNext ? page.getLast().getTest().getId() : null;

        return new ResultListResponse(results, nextCursor, hasNext);
    }

    /**
     * 결과 상세 조회 (GET /results/{id})
     *
     * 본인 소유 검증: userId가 다르면 403
     * report는 CacheService 경유 - 버킷 값으로 disc_cache PK 룩업
     */
    public ResultDetailResponse detail(Long userId, Long testId) {
        DiscResult dr = discResultRepository.findByTestIdWithDetail(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 결과입니다."));

        // 본인 소유 검증
        if (!dr.getTest().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // disc_cache PK 룩업으로 report 조회 (Fetch Join 대신 별도 쿼리로 복잡도 감소 전략)
        ScoringService.Buckets buckets = new ScoringService.Buckets(
                dr.getDBucket(), dr.getIBucket(), dr.getSBucket(), dr.getCBucket()
        );
        String report = cacheService.getReport(buckets);

        return new ResultDetailResponse(
                dr.getTest().getId(),
                dr.getTest().getRaterType(),
                dr.getTest().getLabel(),
                new DiscScores(dr.getDScore(), dr.getIScore(), dr.getSScore(), dr.getCScore()),
                new DiscBuckets(dr.getDBucket(), dr.getIBucket(), dr.getSBucket(), dr.getCBucket()),
                report,
                dr.getTest().getCreatedAt()
        );
    }
}
