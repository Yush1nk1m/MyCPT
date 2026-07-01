package com.mycpt.backend.domain.assessment.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.assessment.entity.AssessmentToken;
import com.mycpt.backend.domain.assessment.repository.AssessmentTokenRepository;
import com.mycpt.backend.domain.result.dto.DiscScoreRequest;
import com.mycpt.backend.domain.result.entity.DiscTest;
import com.mycpt.backend.domain.result.repository.DiscTestRepository;
import com.mycpt.backend.domain.result.service.ScoringService;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 타인 평정 서비스
 *
 * 담당 유즈케이스 (UC-02):
 *  createToken()       -   회원이 일회용 평정 링크 생성
 *  getSubjectInfo()    -   평정자가 링크 접속 시 대상자 정보 + 토큰 유효성 검증
 *  submit()            -   평정 제출: 채점 -> tests/disc_results INSERT -> used=TRUE (단일 트랜잭션)
 *
 * submit() 단일 트랜잭션 설계:
 *  채점(순수 함수) → DiscTest.createForOther() → discTestRepository.save()
 *  → JPA가 tests + disc_tests 두 테이블에 나눠 INSERT
 *  → token.markUsed() + assessmentTokenRepository.save()
 *  중간 단계 예외 시 전체 롤백 → 부분 저장 없음
 */
@Service
public class AssessmentService {

    private final AssessmentTokenRepository assessmentTokenRepository;
    private final DiscTestRepository discTestRepository;
    private final UserRepository userRepository;
    private final ScoringService scoringService;
    private final long tokenTtlDays;

    public AssessmentService(
            AssessmentTokenRepository assessmentTokenRepository,
            DiscTestRepository discTestRepository,
            UserRepository userRepository,
            ScoringService scoringService,
            @Value("${assessment.token.ttl-days:7}") long tokenTtlDays
    ) {
        this.assessmentTokenRepository = assessmentTokenRepository;
        this.discTestRepository = discTestRepository;
        this.userRepository = userRepository;
        this.scoringService = scoringService;
        this.tokenTtlDays = tokenTtlDays;
    }

    // ── DTO 레코드 (내부 전용) ─────────────────────────────────────────────

    /**
     * createToken() 반환 데이터
     */
    public record TokenInfo(String token, LocalDateTime expiresAt) {}

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
    public TokenInfo createToken(Long subjectId, String label) {
        // UserRepository에서 subject 조회 - 존재 보장 (인증 필터 통과 후 호출)
        User subject = userRepository.getReferenceById(subjectId);

        AssessmentToken token = AssessmentToken.create(subject, label, tokenTtlDays);
        assessmentTokenRepository.save(token);

        return new TokenInfo(token.getToken(), token.getExpiresAt());
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
     * @throws jakarta.persistence.EntityNotFoundException      토큰 없음 -> 컨트롤러가 404로 변환
     * @throws BusinessException (TOKEN_USED)                   이미 사용된 토큰
     * @throws BusinessException (EXPIRED_CODE)                 만료된 토큰
     */
    @Transactional(readOnly = true)
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
     * 채점 -> Test INSERT -> DiscTest INSERT -> token.markUsed() + save()
     *
     * @param tokenValue 32자 일회용 토큰
     * @param request    DISC 원점수 (testType, scores)
     */
    @Transactional
    public void submit(String tokenValue, DiscScoreRequest request) {
        AssessmentToken token = findAndValidate(tokenValue);

        ScoringService.Buckets buckets = scoringService.normalize(request);
        DiscScoreRequest.Scores s = request.scores();

        User subject = token.getSubject();
        DiscTest discTest = DiscTest.createForOther(
                subject,
                token.getLabel(),
                s.d(), s.i(), s.s(), s.c(),
                buckets.d(), buckets.i(), buckets.s(), buckets.c()
        );
        discTestRepository.save(discTest);

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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 토큰입니다."));

        // used 먼저 체크 - 만료된 토큰도 used=TRUE면 TOKEN_USED가 더 직관적인 메시지
        if (token.isUsed()) {
            throw new BusinessException(ErrorCode.TOKEN_USED);
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EXPIRED_CODE);
        }

        return token;
    }
}
