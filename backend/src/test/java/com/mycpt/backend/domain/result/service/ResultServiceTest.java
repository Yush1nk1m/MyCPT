package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.result.dto.ResultDetailResponse;
import com.mycpt.backend.domain.result.dto.ResultListResponse;
import com.mycpt.backend.domain.result.dto.ScoreRequest;
import com.mycpt.backend.domain.result.entity.DiscTest;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.result.repository.DiscTestRepository;
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
    @Mock private DiscTestRepository discTestRepository;
    @Mock private UserRepository userRepository;

    private ResultService sut() {
        return new ResultService(scoringService, cacheService, discTestRepository, userRepository);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private static final String REPORT = "## 결과 개요\n테스트 보고서";

    private ScoreRequest validRequest() {
        return new ScoreRequest("DISC", new ScoreRequest.Scores(32, 10, -4, -14));
    }

    private User stubUser() {
        return User.create("kakao-1", "유신", "https://example.com/img.jpg");
    }

    private DiscTest stubDiscTest(Long userId, RaterType raterType) {
        User user = User.create("kakao-" + userId, "유저" + userId, null);
        setId(user, userId);

        DiscTest dt = (raterType == RaterType.SELF)
                ? DiscTest.createForSelf(user, 32, 10, -4, -14, 3, 2, 1, 2)
                : DiscTest.createForOther(user, "테스트용라벨", 32, 10, -4, -14, 3, 2, 1, 2);
        setId(dt, userId * 10);
        return dt;
    }

    private void setId(Object target, Long id) {
        try {
            // DiscTest는 Test를 상속하므로 id 필드는 부모 클래스에 존재
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(target, id);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
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
            given(discTestRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            // DiscTest.id는 DB AUTO_INCREMENT → 저장 전 null. UT에서는 save 호출 횟수만 검증
            sut().save(1L, validRequest());

            // then
            verify(discTestRepository, times(1)).save(any(DiscTest.class));
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

            verify(discTestRepository, never()).save(any());
        }
    }

    // ── list() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("list()")
    class ListResults {

        @Test
        @DisplayName("[UT-ResultSvc-이력조회-성공]")
        void 이력조회_성공() {
            // given: size=5, cursor=null → 리포지토리에서 3개 반환 (hasNext=false)
            List<DiscTest> rows = List.of(
                    stubDiscTest(1L, RaterType.SELF),
                    stubDiscTest(2L, RaterType.SELF),
                    stubDiscTest(3L, RaterType.OTHER)
            );
            given(discTestRepository.findByUserIdWithCursor(
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
            // given: size=2, 리포지토리에서 size+1=3개 반환 → hasNext=true
            List<DiscTest> rows = List.of(
                    stubDiscTest(1L, RaterType.SELF),
                    stubDiscTest(2L, RaterType.SELF),
                    stubDiscTest(3L, RaterType.SELF)
            );
            given(discTestRepository.findByUserIdWithCursor(
                    eq(1L), isNull(), isNull(), any(PageRequest.class)
            )).willReturn(rows);

            // when
            ResultListResponse response = sut().list(1L, null, null, 2);

            // then
            assertThat(response.results()).hasSize(2);
            assertThat(response.hasNext()).isTrue();
            // nextCursor = page.getLast().getId() = 2L * 10 = 20L
            assertThat(response.nextCursor()).isEqualTo(20L);
        }

        @Test
        @DisplayName("[UT-ResultSvc-이력조회-마지막페이지]")
        void 이력조회_마지막페이지() {
            // given: size=5, 리포지토리에서 2개만 반환 → hasNext=false
            List<DiscTest> rows = List.of(
                    stubDiscTest(1L, RaterType.SELF),
                    stubDiscTest(2L, RaterType.OTHER)
            );
            given(discTestRepository.findByUserIdWithCursor(
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
            // given: raterType 파라미터가 리포지토리로 올바르게 전달되는지만 검증
            // 리포지토리 필터 동작 자체는 DiscTestRepositoryTest에서 검증
            List<DiscTest> rows = List.of(stubDiscTest(1L, RaterType.SELF));
            given(discTestRepository.findByUserIdWithCursor(
                    eq(1L), eq(RaterType.SELF), isNull(), any(PageRequest.class)
            )).willReturn(rows);

            // when
            ResultListResponse response = sut().list(1L, RaterType.SELF, null, 5);

            // then
            assertThat(response.results()).hasSize(1);
            assertThat(response.results().getFirst().raterType()).isEqualTo(RaterType.SELF);
            verify(discTestRepository).findByUserIdWithCursor(
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
            DiscTest dt = stubDiscTest(1L, RaterType.SELF);
            given(discTestRepository.findByTestIdWithDetail(10L)).willReturn(Optional.of(dt));
            given(cacheService.getReport(any())).willReturn(REPORT);

            // when
            ResultDetailResponse response = sut().detail(1L, 10L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.raterType()).isEqualTo(RaterType.SELF);
            assertThat(response.report()).isEqualTo(REPORT);
        }

        @Test
        @DisplayName("[UT-ResultSvc-상세조회-권한없음]")
        void 상세조회_권한없음() {
            // given: userId=2L이 userId=1L의 결과 조회 시도
            DiscTest dt = stubDiscTest(1L, RaterType.SELF);
            given(discTestRepository.findByTestIdWithDetail(10L)).willReturn(Optional.of(dt));

            // when
            assertThatThrownBy(() -> sut().detail(2L, 10L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.FORBIDDEN));

            verify(cacheService, never()).getReport(any());
        }

        @Test
        @DisplayName("[UT-ResultSvc-상세조회-존재하지않는ID]")
        void 상세조회_존재하지않는ID() {
            // given
            given(discTestRepository.findByTestIdWithDetail(999L)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> sut().detail(1L, 999L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.NOT_FOUND));
        }
    }
}