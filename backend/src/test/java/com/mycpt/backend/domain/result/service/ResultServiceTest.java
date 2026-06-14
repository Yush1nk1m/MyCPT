package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.result.dto.ResultDetailResponse;
import com.mycpt.backend.domain.result.dto.ResultListResponse;
import com.mycpt.backend.domain.result.dto.ScoreRequest;
import com.mycpt.backend.domain.result.entity.DiscResult;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.result.repository.DiscResultRepository;
import com.mycpt.backend.domain.result.repository.TestRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResultService 단위 테스트")
class ResultServiceTest {

    @Mock private ScoringService scoringService;
    @Mock private CacheService cacheService;
    @Mock private TestRepository testRepository;
    @Mock private DiscResultRepository discResultRepository;
    @Mock private UserRepository userRepository;

    private ResultService sut() {
        return new ResultService(scoringService, cacheService, testRepository, discResultRepository, userRepository);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private static final String REPORT = "## 결과 개요\n테스트 보고서";

    private ScoreRequest validRequest() {
        return new ScoreRequest("DISC", new ScoreRequest.Scores(32, 10, -4, -14));
    }

    private User stubUser() {
        return User.create("kakao-1", "유신", "https://example.com/img.jpg");
    }

    private DiscResult stubDiscResult(Long userId, RaterType raterType) {
        User user = User.create("kakao-" + userId, "유저" + userId, null);
        // 리플렉션으로 id 주입 - DB AUTO_INCREMENT를 UT에서 시뮬레이션
        setId(user, userId);

        com.mycpt.backend.domain.result.entity.Test test = (raterType == RaterType.SELF)
                ? com.mycpt.backend.domain.result.entity.Test.createForSelf(user, "DISC")
                : com.mycpt.backend.domain.result.entity.Test.createForOther(user, "DISC", "테스트용라벨");
        setId(test, userId * 10);   // 단순 구분용 id

        return DiscResult.create(test, 32, 10, -4, -14, 3, 2, 1, 2);
    }

    /**
     * id 필드 리플렉션 주입 공통 헬퍼
     */
    private void setId(Object target, Long id) {
        try {
            var field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── save() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("[UT-ResultSvc-저장-성공]")
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
        @DisplayName("[UT-ResultSvc-저장-원점수오류]")
        void 저장_원점수오류() {
            // given
            given(scoringService.normalize(any()))
                    .willThrow(new BusinessException(ErrorCode.INVALID_SCORE, "D+I+S+C 합계가 올바르지 않습니다."));

            // when
            assertThatThrownBy(() -> sut().save(1L, validRequest()))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_SCORE);
                        assertThat(be.getMessage()).contains("D+I+S+C 합계가 올바르지 않습니다.");
                    });

            verify(testRepository, never()).save(any());
            verify(discResultRepository, never()).save(any());
        }
    }

    // ── list() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("list()")
    class ListResults {

        @Test
        @DisplayName("[UT-ResultSvc-이력조회-성공]")
        void 이력조회_성공() {
            // given: size=5, cursor=null -> 리포지토리에서 5개 반환 (hasNext=false)
            List<DiscResult> rows = List.of(
                    stubDiscResult(1L, RaterType.SELF),
                    stubDiscResult(2L, RaterType.SELF),
                    stubDiscResult(3L, RaterType.OTHER)
            );
            given(discResultRepository.findByUserIdWithCursor(
                    eq(1L), isNull(), isNull(), any(PageRequest.class)
            )).willReturn(rows);

            // when
            ResultListResponse response = sut().list(1L, null, null, 5);

            // then
            assertThat(response.results()).hasSize(3);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("[UT-ResultSvc-이력조회-다음페이지존재]")
        void 이력조회_다음페이지존재() {
            // given: size=2, 리포지토리에서 size+1=3개 반환 -> hasNext=true
            List<DiscResult> rows = List.of(
                    stubDiscResult(1L, RaterType.SELF),
                    stubDiscResult(2L, RaterType.SELF),
                    stubDiscResult(3L, RaterType.SELF)
            );
            given(discResultRepository.findByUserIdWithCursor(
                    eq(1L), isNull(), isNull(), any(PageRequest.class)
            )).willReturn(rows);

            // when
            ResultListResponse response = sut().list(1L, null, null, 2);

            // then
            assertThat(response.results()).hasSize(2);
            assertThat(response.hasNext()).isTrue();
            // nextCursor = page.getLast().getTest().getId() = 2L * 10 = 20L
            assertThat(response.nextCursor()).isEqualTo(20L);
        }

        @Test
        @DisplayName("[UT-ResultSvc-이력조회-마지막페이지]")
        void 이력조회_마지막페이지() {
            // given: size=5, 리포지토리에서 2개만 반환 -> hasNext=false
            List<DiscResult> rows = List.of(
                    stubDiscResult(1L, RaterType.SELF),
                    stubDiscResult(2L, RaterType.OTHER)
            );
            given(discResultRepository.findByUserIdWithCursor(
                    eq(1L), isNull(), eq(99L), any(PageRequest.class)
            )).willReturn(rows);

            // when
            ResultListResponse response = sut().list(1L, null, 99L, 5);

            // then
            assertThat(response.results()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("[UT-ResultSvc-이력조회-raterType필터]")
        void 이력조회_raterType필터() {
            // given: raterType=SELF 필터 전달 -> 리포지토리가 SELF만 반환한다고 가정
            // 리포지토리 필터 동작 자체는 DiscResultRepositoryTest에서 검증
            // 여기서는 파라미터가 올바르게 전달되는지만 검증
            List<DiscResult> rows = List.of(stubDiscResult(1L, RaterType.SELF));
            given(discResultRepository.findByUserIdWithCursor(
                    eq(1L), eq(RaterType.SELF), isNull(), any(PageRequest.class)
            )).willReturn(rows);

            // when
            ResultListResponse response = sut().list(1L, RaterType.SELF, null, 5);

            // then
            assertThat(response.results()).hasSize(1);
            assertThat(response.results().getFirst().raterType()).isEqualTo(RaterType.SELF);
            // raterType 파라미터가 리포지토리로 올바르게 전달됐는지 검증
            verify(discResultRepository).findByUserIdWithCursor(
                    eq(1L), eq(RaterType.SELF), isNull(), any(PageRequest.class)
            );
        }
    }

    // ── detail() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("detail()")
    class DetailResult {

        @Test
        @DisplayName("[UT-ResultSvc-상세조회-성공]")
        void 상세조회_성공() {
            // given: userId=1L 본인 결과 조회
            DiscResult dr = stubDiscResult(1L, RaterType.SELF);
            given(discResultRepository.findByTestIdWithDetail(10L)).willReturn(Optional.of(dr));
            given(cacheService.getReport(any())).willReturn(REPORT);

            // when
            ResultDetailResponse response = sut().detail(1L, 10L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.raterType()).isEqualTo(RaterType.SELF);
            assertThat(response.report()).isEqualTo(REPORT);
            verify(cacheService, times(1)).getReport(any());
        }

        @Test
        @DisplayName("[UT-ResultSvc-상세조회-권한없음]")
        void 상세조회_권한없음() {
            // given: DB에는 userId=1L의 결과가 있으나 userId=2로 접근
            DiscResult dr = stubDiscResult(1L, RaterType.SELF);
            given(discResultRepository.findByTestIdWithDetail(10L)).willReturn(Optional.of(dr));

            // when
            assertThatThrownBy(() -> sut().detail(2L, 10L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    });

            // 권한 없으면 CacheService 미호출
            verify(cacheService, never()).getReport(any());
        }

        @Test
        @DisplayName("[UT-ResultSvc-상세조회-존재하지않는ID]")
        void 상세조회_존재하지않는ID() {
            // given
            given(discResultRepository.findByTestIdWithDetail(999L)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> sut().detail(1L, 999L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                        assertThat(be.getMessage()).contains("존재하지 않는 결과입니다.");
                    });
        }
    }
}