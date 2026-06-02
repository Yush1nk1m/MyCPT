package com.mycpt.backend.domain.assessment.service;

import com.mycpt.backend.domain.assessment.entity.AssessmentToken;
import com.mycpt.backend.domain.assessment.repository.AssessmentTokenRepository;
import com.mycpt.backend.domain.result.dto.ScoreRequest;
import com.mycpt.backend.domain.result.entity.DiscResult;
import com.mycpt.backend.domain.result.entity.Test;
import com.mycpt.backend.domain.result.repository.DiscResultRepository;
import com.mycpt.backend.domain.result.repository.TestRepository;
import com.mycpt.backend.domain.result.service.ScoringService;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.global.exception.TokenAlreadyUsedException;
import com.mycpt.backend.global.exception.TokenExpiredException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 타인 평정 서비스
 *
 * 담당 유즈케이스 (UC-02):
 *  createToken()       -   회원이 일회용 평정 링크 생성
 *  getSubjectInfo()    -   평정자가 링크 접속 시 대상자 정보 + 토큰 유효성 검증
 *  submit()            -   평정 제출: 채점 -> tests/disc_results INSERT -> used=TRUE (단일 트랜잭션)
 *
 * submit()의 단일 트랜잭션 설계:
 *  채점(순수 함수) -> Test INSERT -> DiscResult INSERT -> markUsed() + save()
 *  중간 단계에서 예외 발생 시 전체 롤백 -> 부분 저장 없음
 */
@Service
public class AssessmentService {

    private final AssessmentTokenRepository assessmentTokenRepository;
    private final TestRepository testRepository;
    private final DiscResultRepository discResultRepository;
    private final UserRepository userRepository;
    private final ScoringService scoringService;
    private final long tokenTtlDays;

    public AssessmentService(
            AssessmentTokenRepository assessmentTokenRepository,
            TestRepository testRepository,
            DiscResultRepository discResultRepository,
            UserRepository userRepository,
            ScoringService scoringService,
            @Value("${assessment.token.ttl-days:7}") long tokenTtlDays
    ) {
        this.assessmentTokenRepository = assessmentTokenRepository;
        this.testRepository = testRepository;
        this.discResultRepository = discResultRepository;
        this.userRepository = userRepository;
        this.scoringService = scoringService;
        this.tokenTtlDays = tokenTtlDays;
    }

    // ── DTO 레코드 (내부 전용) ─────────────────────────────────────────────

    /**
     * createToken() 반환 데이터
     */
    public record TokenResult(String token, LocalDateTime expiresAt) {}

    /**
     * getSubjectInfo() 반환 데이터
     * GET /assessments/{token} 응답에 사용
     */
    public record SubjectInfo(String subjectNickname, String subjectProfileImageUrl) {}

    // ── 퍼블릭 메서드 ──────────────────────────────────────────────────────

    /**
     * 타인 평정 링크 생성
     * POST /assessments - 회원 전용
     *
     * @param subjectId 링크를 생성한 회원 ID (SecurityContext에서 추출)
     * @param label     평정자 식별 라벨 (nullable, 최대 30자)
     * @return 생성된 토큰 문자열 + 만료 시각
     */
    @Transactional
    public TokenResult createToken(Long subjectId, String label) {
        // UserRepository에서 subject 조회 - 존재 보장 (인증 필터 통과 후 호출)
        User subject = userRepository.getReferenceById(subjectId);

        AssessmentToken token = AssessmentToken.create(subject, label, tokenTtlDays);
        assessmentTokenRepository.save(token);

        return new TokenResult(token.getToken(), token.getExpiresAt());
    }

    /**
     * 평정 링크 접속 - 토큰 유효성 검증 후 대상자 정보 반환
     * GET /assessments/{token} - 비회원 가능
     *
     * 유효성 검증 순서:
     *  1. 토큰 존재 여부 (404)
     *  2. used=TRUE 여부 (400 TOKEN_USED)
     *  3. 만료 여부 (400 EXPIRED_CODE)
     *
     * @throws jakarta.persistence.EntityNotFoundException                  토큰 없음 -> 컨트롤러가 404로 변환
     * @throws com.mycpt.backend.global.exception.TokenAlreadyUsedException 이미 사용된 토큰
     * @throws com.mycpt.backend.global.exception.TokenExpiredException     만료된 토큰
     */
    public SubjectInfo getSubjectInfo(String tokenValue) {
        AssessmentToken token = findAndValidate(tokenValue);
        User subject = token.getSubject();
        return new SubjectInfo(subject.getNickname(), subject.getProfileImageUrl());
    }

    /**
     * 타인 평정 제출
     * POST /assessments/{token}/submit - 비회원 가능
     * <p>
     * 단일 트랜잭션:
     * 채점 -> Test INSERT -> DiscResult INSERT -> token.markUsed() + save()
     *
     * @param tokenValue 32자 일회용 토큰
     * @param request    DISC 원점수 (testType, scores)
     */
    @Transactional
    public void submit(String tokenValue, ScoreRequest request) {
        AssessmentToken token = findAndValidate(tokenValue);

        // 1. 채점 - ScoringService 재사용 (원점수 검증 + 버킷 정규화 포함)
        ScoringService.Buckets buckets = scoringService.normalize(request);
        ScoreRequest.Scores s = request.scores();

        // 2. tests INSERT (rater_type=OTHER, label=토큰의 label)
        //    subject_id: 피평정자 (결과가 귀속될 회원)
        Test test = Test.createForOther(token.getSubject(), request.testType(), token.getLabel());
        testRepository.save(test);

        // 3. disc_results INSERT
        //    disc_cache FK 참조: (d_bucket, i_bucket, s_bucket, c_bucket)
        //    주의: disc_cache 행이 없으면 FK 위반 -> CacheService.getReport() 메서드로 캐시 미리 생성 필요
        //    현재 설계: submit 시점에는 캐시 미생성. 결과 조회 시(GET /results/{id})에 CacheService 경유
        DiscResult discResult = DiscResult.create(
                test,
                s.d(), s.i(), s.s(), s.c(),
                buckets.d(), buckets.i(), buckets.s(), buckets.c()
        );
        discResultRepository.save(discResult);

        // 4. 토큰 소비 - used=TRUE
        token.markUsed();
        assessmentTokenRepository.save(token);
    }

    // ── private ────────────────────────────────────────────────────────────

    /**
     * 토큰 조회 + 유효성 검증 공통 로직
     * getSubjectInfo / submit 양쪽에서 동일한 검증 순서를 사용
     */
    private AssessmentToken findAndValidate(String tokenValue) {
        // findByToken이 empty면 NoSuchElementException -> 컨트롤러에서 404로 변환
        AssessmentToken token = assessmentTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("존재하지 않는 토큰입니다."));

        // used 먼저 체크 - 만료된 토큰도 used=TRUE면 TOKEN_USED가 더 직관적인 메시지
        if (token.isUsed()) {
            throw new TokenAlreadyUsedException();
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }

        return token;
    }
}
