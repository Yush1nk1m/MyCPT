package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.domain.result.dto.ScoreRequest;
import com.mycpt.backend.domain.result.entity.DiscResult;
import com.mycpt.backend.domain.result.entity.Test;
import com.mycpt.backend.domain.result.repository.DiscResultRepository;
import com.mycpt.backend.domain.result.repository.TestRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
 *
 * TODO (3주차): 결과 이력 조회, 결과 상세 조회
 */
@Service
@RequiredArgsConstructor
public class ResultSaveService {

    private final ScoringService scoringService;
    private final TestRepository testRepository;
    private final DiscResultRepository discResultRepository;
    private final UserRepository userRepository;

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
}
