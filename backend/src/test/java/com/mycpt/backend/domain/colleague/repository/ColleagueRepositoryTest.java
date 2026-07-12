package com.mycpt.backend.domain.colleague.repository;

import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.JpaTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ColleagueRepository 슬라이스 테스트")
class ColleagueRepositoryTest extends JpaTestSupport {

    @Autowired private ColleagueRepository colleagueRepository;
    @Autowired private UserRepository userRepository;

    @Nested
    @DisplayName("countByUserId()")
    class CountByUserId {

        @Test
        @DisplayName("[ST-ColleagueRepo-사용자별카운트-성공]")
        void 사용자별카운트_성공() {
            // given
            User userA = userRepository.save(User.create("kakao-a", "A", null));
            User userB = userRepository.save(User.create("kakao-b", "B", null));
            User userC = userRepository.save(User.create("kakao-c", "C", null));
            colleagueRepository.save(Colleague.create(userA, userB));
            colleagueRepository.save(Colleague.create(userC, userA));

            // when
            long count = colleagueRepository.countByUserId(userA.getId());

            // then
            assertThat(count).isEqualTo(2);
        }
    }
}