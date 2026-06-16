package com.mycpt.backend.domain.assessment.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.assessment.entity.AssessmentToken;
import com.mycpt.backend.domain.assessment.repository.AssessmentTokenRepository;
import com.mycpt.backend.domain.result.dto.DiscScoreRequest;
import com.mycpt.backend.domain.result.repository.DiscTestRepository;
import com.mycpt.backend.domain.result.service.ScoringService;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssessmentService 단위 테스트")
class AssessmentServiceTest {

    @Mock private AssessmentTokenRepository assessmentTokenRepository;
    @Mock private DiscTestRepository discTestRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScoringService scoringService;

    private AssessmentService sut() {
        return new AssessmentService(
                assessmentTokenRepository,
                discTestRepository,
                userRepository,
                scoringService,
                7L
        );
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private AssessmentToken validToken() {
        return AssessmentToken.create(stubUser(), "여자친구", 7);
    }

    private AssessmentToken usedToken() {
        AssessmentToken t = validToken();
        t.markUsed();
        return t;
    }

    private AssessmentToken expiredToken() {
        AssessmentToken t = validToken();
        try {
            var field = AssessmentToken.class.getDeclaredField("expiresAt");
            field.setAccessible(true);
            field.set(t, LocalDateTime.now().minusDays(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    private User stubUser() {
        return User.create("kakao-123", "유신", null);
    }

    private DiscScoreRequest validRequest() {
        return new DiscScoreRequest(new DiscScoreRequest.Scores(32, 10, -4, -14));
    }

    // ── createToken() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createToken()")
    class CreateToken {

        @Test
        @DisplayName("[UT-AssessmentSvc-토큰생성-성공]")
        void 토큰생성_성공() {
            // given
            given(userRepository.getReferenceById(1L)).willReturn(stubUser());
            given(assessmentTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            AssessmentService.TokenInfo result = sut().createToken(1L, "여자친구");

            // then
            assertThat(result.token()).hasSize(32);
            assertThat(result.expiresAt()).isAfter(LocalDateTime.now().plusDays(6));
            verify(assessmentTokenRepository, times(1)).save(any());
        }
    }

    // ── getSubjectInfo() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSubjectInfo()")
    class GetSubjectInfo {

        @Test
        @DisplayName("[UT-AssessmentSvc-링크접속-성공]")
        void 링크접속_성공() {
            // given
            given(assessmentTokenRepository.findByToken("valid")).willReturn(Optional.of(validToken()));

            // when
            AssessmentService.SubjectInfo info = sut().getSubjectInfo("valid");

            // then
            assertThat(info.subjectNickname()).isEqualTo("유신");
        }

        @Test
        @DisplayName("[UT-AssessmentSvc-링크접속-토큰없음]")
        void 링크접속_토큰없음() {
            given(assessmentTokenRepository.findByToken("invalid")).willReturn(Optional.empty());

            assertThatThrownBy(() -> sut().getSubjectInfo("invalid"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.NOT_FOUND));
        }

        @Test
        @DisplayName("[UT-AssessmentSvc-링크접속-이미사용된토큰]")
        void 링크접속_이미사용된토큰() {
            given(assessmentTokenRepository.findByToken("used")).willReturn(Optional.of(usedToken()));

            assertThatThrownBy(() -> sut().getSubjectInfo("used"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.TOKEN_USED));
        }

        @Test
        @DisplayName("[UT-AssessmentSvc-링크접속-만료된토큰]")
        void 링크접속_만료된토큰() {
            given(assessmentTokenRepository.findByToken("expired")).willReturn(Optional.of(expiredToken()));

            assertThatThrownBy(() -> sut().getSubjectInfo("expired"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.EXPIRED_CODE));
        }
    }

    // ── submit() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("submit()")
    class Submit {

        @Test
        @DisplayName("[UT-AssessmentSvc-평정제출-성공]")
        void 평정제출_성공() {
            // given
            AssessmentToken token = validToken();
            given(assessmentTokenRepository.findByToken("valid")).willReturn(Optional.of(token));
            given(scoringService.normalize(any())).willReturn(new ScoringService.Buckets(2, 1, 3, 2));
            given(discTestRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(assessmentTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            sut().submit("valid", validRequest());

            // then
            assertThat(token.isUsed()).isTrue();
            verify(discTestRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("[UT-AssessmentSvc-평정제출-이미사용된토큰]")
        void 평정제출_이미사용된토큰() {
            // given
            given(assessmentTokenRepository.findByToken("used")).willReturn(Optional.of(usedToken()));

            // when
            assertThatThrownBy(() -> sut().submit("used", validRequest()))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.TOKEN_USED));

            verify(discTestRepository, never()).save(any());
        }
    }
}