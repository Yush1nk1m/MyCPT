package com.mycpt.backend.domain.result.repository;

import com.mycpt.backend.domain.result.entity.DiscResult;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.JpaTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Sql(scripts = "/sql/disc_cache_seed.sql")
@DisplayName("DiscResultRepository 슬라이스 테스트")
class DiscResultRepositoryTest extends JpaTestSupport {

    @Autowired
    private DiscResultRepository discResultRepository;
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.create("kakao-1", "유신", null));
    }

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────────

    private DiscResult saveDiscResult(User user, RaterType raterType, String label) {
        com.mycpt.backend.domain.result.entity.Test test = (raterType == RaterType.SELF)
                ? com.mycpt.backend.domain.result.entity.Test.createForSelf(user, "DISC")
                : com.mycpt.backend.domain.result.entity.Test.createForOther(user, "DISC", label);
        testRepository.save(test);

        DiscResult dr = DiscResult.create(test, 32, 10, -4, -14, 3, 2, 1, 2);
        return discResultRepository.save(dr);
    }

    // ── findByUserIdWithCursor() ──────────────────────────────────────────────

    @Nested
    @DisplayName("findByUserIdWithCursor()")
    class FindByUserIdWithCursor {

        @Test
        @DisplayName("[ST-DiscResultRepo-커서페이지네이션-성공]")
        void 커서페이지네이션_성공() {
            // given
            saveDiscResult(savedUser, RaterType.SELF, null);
            saveDiscResult(savedUser, RaterType.SELF, null);
            saveDiscResult(savedUser, RaterType.OTHER, "친구");

            // when
            List<DiscResult> results = discResultRepository.findByUserIdWithCursor(
                    savedUser.getId(), null, null, PageRequest.of(0, 6)
            );

            // then
            assertThat(results).hasSize(3);
            // 최신순(id DESC) 정렬 검증
            assertThat(results.get(0).getTest().getId())
                    .isGreaterThan(results.get(1).getTest().getId());
        }

        @Test
        @DisplayName("[ST-DiscResultRepo-커서페이지네이션-커서검증]")
        void 커서페이지네이션_커서검증() {
            // given
            DiscResult first = saveDiscResult(savedUser, RaterType.SELF, null);
            DiscResult second = saveDiscResult(savedUser, RaterType.SELF, null);
            saveDiscResult(savedUser, RaterType.SELF, null);    // third - cursor 기준점

            // when
            // AUTO_INCREMENT 순서: first.id < second.id < third.id
            // cursor = second.id -> id < second.id 인 first만 반환
            Long cursor = second.getTest().getId();

            List<DiscResult> results = discResultRepository.findByUserIdWithCursor(
                    savedUser.getId(), null, cursor, PageRequest.of(0, 6)
            );

            // then
            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getTest().getId())
                    .isEqualTo(first.getTest().getId());
        }

        @Test
        @DisplayName("[ST-DiscResultRepo-raterType필터-SELF]")
        void raterType필터_SELF() {
            // given
            saveDiscResult(savedUser, RaterType.SELF, null);
            saveDiscResult(savedUser, RaterType.SELF, null);
            saveDiscResult(savedUser, RaterType.OTHER, "친구");

            // when
            List<DiscResult> results = discResultRepository.findByUserIdWithCursor(
                    savedUser.getId(), RaterType.SELF, null, PageRequest.of(0, 6)
            );

            // then
            assertThat(results).hasSize(2);
            assertThat(results)
                    .allMatch(dr -> dr.getTest().getRaterType() == RaterType.SELF);
        }

        @Test
        @DisplayName("[ST-DiscResultRepo-raterType필터-null]")
        void raterType필터_null() {
            // given
            saveDiscResult(savedUser, RaterType.SELF, null);
            saveDiscResult(savedUser, RaterType.OTHER, "친구");

            // when
            List<DiscResult> results = discResultRepository.findByUserIdWithCursor(
                    savedUser.getId(), null, null, PageRequest.of(0, 6)
            );

            // then
            assertThat(results).hasSize(2);
        }
    }

    // ── findByTestIdWithDetail() ──────────────────────────────────────────────

    @Nested
    @DisplayName("findByTestIdWithDetail()")
    class FindByTestIdWithDetail {

        @Test
        @DisplayName("[ST-DiscResultRepo-상세조회-JoinFetch]")
        void 상세조회_JoinFetch() {
            // given
            DiscResult savedDiscResult = saveDiscResult(savedUser, RaterType.SELF, null);
            Long testId = savedDiscResult.getTest().getId();
            discResultRepository.flush();

            // when
            Optional<DiscResult> result = discResultRepository.findByTestIdWithDetail(testId);

            // then
            assertThat(result).isPresent();
            // JOIN FETCH로 인해 영속성 컨텍스트 밖에서도 test 접근 가능. LazyInitializationException 미발생 검증
            assertThatCode(() -> {
                String raterType = result.get().getTest().getRaterType().name();
                Long userId = result.get().getTest().getUser().getId();
                assertThat(raterType).isEqualTo("SELF");
                assertThat(userId).isEqualTo(savedUser.getId());
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[ST-DiscResultRepo-상세조회-존재하지않는ID]")
        void 상세조회_존재하지않는ID() {
            Optional<DiscResult> result = discResultRepository.findByTestIdWithDetail(999L);

            assertThat(result).isEmpty();
        }
    }
}

