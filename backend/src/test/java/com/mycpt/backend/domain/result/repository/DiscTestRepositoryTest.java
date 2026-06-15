package com.mycpt.backend.domain.result.repository;

import com.mycpt.backend.domain.result.entity.DiscTest;
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
@DisplayName("DiscTestRepository 슬라이스 테스트")
class DiscTestRepositoryTest extends JpaTestSupport {

    @Autowired private DiscTestRepository discTestRepository;
    @Autowired private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.create("kakao-1", "유신", null));
    }

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────────

    private DiscTest saveDiscTest(User user, RaterType raterType, String label) {
        DiscTest dt = (raterType == RaterType.SELF)
                ? DiscTest.createForSelf(user, 32, 10, -4, -14, 3, 2, 1, 2)
                : DiscTest.createForOther(user, label, 32, 10, -4, -14, 3, 2, 1, 2);
        return discTestRepository.save(dt);
    }

    // ── findByUserIdWithCursor() ──────────────────────────────────────────────

    @Nested
    @DisplayName("findByUserIdWithCursor()")
    class FindByUserIdWithCursor {

        @Test
        @DisplayName("[ST-DiscTestRepo-커서페이지네이션-성공]")
        void 커서페이지네이션_성공() {
            // given
            saveDiscTest(savedUser, RaterType.SELF, null);
            saveDiscTest(savedUser, RaterType.SELF, null);
            saveDiscTest(savedUser, RaterType.OTHER, "친구");

            // when
            List<DiscTest> results = discTestRepository.findByUserIdWithCursor(
                    savedUser.getId(), null, null, PageRequest.of(0, 6)
            );

            // then
            assertThat(results).hasSize(3);
            // 최신순(id DESC) 정렬 검증
            assertThat(results.get(0).getId())
                    .isGreaterThan(results.get(1).getId());
        }

        @Test
        @DisplayName("[ST-DiscTestRepo-커서페이지네이션-커서검증]")
        void 커서페이지네이션_커서검증() {
            // given
            DiscTest first  = saveDiscTest(savedUser, RaterType.SELF, null);
            DiscTest second = saveDiscTest(savedUser, RaterType.SELF, null);
            saveDiscTest(savedUser, RaterType.SELF, null); // third — cursor 기준점

            // when: cursor = second.id → id < second.id 인 first만 반환
            Long cursor = second.getId();
            List<DiscTest> results = discTestRepository.findByUserIdWithCursor(
                    savedUser.getId(), null, cursor, PageRequest.of(0, 6)
            );

            // then
            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(first.getId());
        }

        @Test
        @DisplayName("[ST-DiscTestRepo-raterType필터-SELF]")
        void raterType필터_SELF() {
            // given
            saveDiscTest(savedUser, RaterType.SELF, null);
            saveDiscTest(savedUser, RaterType.SELF, null);
            saveDiscTest(savedUser, RaterType.OTHER, "친구");

            // when
            List<DiscTest> results = discTestRepository.findByUserIdWithCursor(
                    savedUser.getId(), RaterType.SELF, null, PageRequest.of(0, 6)
            );

            // then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(dt -> dt.getRaterType() == RaterType.SELF);
        }

        @Test
        @DisplayName("[ST-DiscTestRepo-raterType필터-null]")
        void raterType필터_null() {
            // given
            saveDiscTest(savedUser, RaterType.SELF, null);
            saveDiscTest(savedUser, RaterType.OTHER, "친구");

            // when
            List<DiscTest> results = discTestRepository.findByUserIdWithCursor(
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
        @DisplayName("[ST-DiscTestRepo-상세조회-JoinFetch]")
        void 상세조회_JoinFetch() {
            // given
            DiscTest saved = saveDiscTest(savedUser, RaterType.SELF, null);
            discTestRepository.flush();

            // when
            Optional<DiscTest> result = discTestRepository.findByTestIdWithDetail(saved.getId());

            // then
            assertThat(result).isPresent();
            // @Inheritance(JOINED) 구조에서 user는 tests 테이블 소속 → 접근 가능 검증
            assertThat(result.get().getUser().getNickname()).isEqualTo("유신");
        }

        @Test
        @DisplayName("[ST-DiscTestRepo-상세조회-존재하지않는ID]")
        void 상세조회_존재하지않는ID() {
            // when
            Optional<DiscTest> result = discTestRepository.findByTestIdWithDetail(999L);

            // then
            assertThat(result).isEmpty();
        }
    }
}