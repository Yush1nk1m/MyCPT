package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.domain.result.dto.ScoreRequest;
import com.mycpt.backend.domain.result.entity.DiscResult;
import com.mycpt.backend.domain.result.repository.DiscResultRepository;
import com.mycpt.backend.domain.result.repository.TestRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.global.exception.InvalidScoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResultSaveService 단위 테스트")
class ResultSaveServiceTest {

    @Mock private ScoringService scoringService;
    @Mock private TestRepository testRepository;
    @Mock private DiscResultRepository discResultRepository;
    @Mock private UserRepository userRepository;

    private ResultSaveService sut() {
        return new ResultSaveService(scoringService, testRepository, discResultRepository, userRepository);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private ScoreRequest validRequest() {
        return new ScoreRequest("DISC", new ScoreRequest.Scores(32, 10, -4, -14));
    }

    private User stubUser() {
        return User.create("kakao-1", "유신", "https://example.com/img.jpg");
    }

    // ── save() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("[UT-ResultSaveSvc-저장-성공]")
        void 저장_성공() {
            // given
            given(userRepository.getReferenceById(1L)).willReturn(stubUser());
            given(scoringService.normalize(any())).willReturn(new ScoringService.Buckets(3, 2, 1, 2));
            given(testRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(discResultRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            // Test.id는 DB AUTO_INCREMENT이므로 저장 전 null -> resultId도 null
            // 실제 통합 환경에서는 DB가 채워주므로 UT에서는 save 호출 횟수만 검증
            sut().save(1L, validRequest());

            // then
            verify(testRepository, times(1)).save(any(com.mycpt.backend.domain.result.entity.Test.class));
            verify(discResultRepository, times(1)).save(any(DiscResult.class));
        }

        @Test
        @DisplayName("[UT-ResultSaveSvc-저장-원점수오류]")
        void 저장_원점수오류() {
            // given
            given(scoringService.normalize(any()))
                    .willThrow(new com.mycpt.backend.global.exception.InvalidScoreException("D+I+S+C 합계가 올바르지 않습니다."));

            // when
            assertThatThrownBy(() -> sut().save(1L, validRequest()))
            // then
                    .isInstanceOf(InvalidScoreException.class);

            verify(testRepository, never()).save(any());
            verify(discResultRepository, never()).save(any());
        }
    }
}