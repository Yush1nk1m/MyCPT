package com.mycpt.backend.domain.colleague.repository;

import com.mycpt.backend.domain.colleague.entity.PeerCode;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.JpaTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PeerCodeRepository 슬라이스 테스트")
class PeerCodeRepositoryTest extends JpaTestSupport {

    @Autowired private PeerCodeRepository peerCodeRepository;
    @Autowired private UserRepository userRepository;

    @Nested
    @DisplayName("deleteByUserId()")
    class DeleteByUserId {

        @Test
        @DisplayName("[ST-PeerCodeRepo-사용자별삭제-성공]")
        void 사용자별삭제_성공() {
            // given
            User user = userRepository.save(User.create("kakao-a", "A", null));
            peerCodeRepository.save(PeerCode.create(user, 7L));

            // when
            peerCodeRepository.deleteByUserId(user.getId());

            // then
            Optional<PeerCode> result = peerCodeRepository.findByUserId(user.getId());
            assertThat(result).isEmpty();
        }
    }

}