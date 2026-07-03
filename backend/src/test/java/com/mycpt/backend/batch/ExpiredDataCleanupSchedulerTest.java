package com.mycpt.backend.batch;

import com.mycpt.backend.domain.assessment.entity.AssessmentToken;
import com.mycpt.backend.domain.assessment.repository.AssessmentTokenRepository;
import com.mycpt.backend.domain.colleague.entity.PeerCode;
import com.mycpt.backend.domain.colleague.repository.PeerCodeRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("ExpiredDataCleanupScheduler 통합 테스트")
class ExpiredDataCleanupSchedulerTest extends IntegrationTestSupport {

    @Autowired ExpiredDataCleanupScheduler scheduler;
    @Autowired PeerCodeRepository peerCodeRepository;
    @Autowired AssessmentTokenRepository assessmentTokenRepository;
    @Autowired UserRepository userRepository;

    private User stubUser(String suffix) {
        return userRepository.save(User.create("kakao-cleanup-" + suffix, suffix, null));
    }

    @Test
    @DisplayName("[IT-ExpiredDataCleanupScheduler-만료데이터삭제-유효데이터보존]")
    void 만료데이터삭제_유효데이터보존() {
        // given
        PeerCode expiredPeerCode = PeerCode.create(stubUser("peer-expired"), 7L);
        setField(expiredPeerCode, "expiresAt", LocalDateTime.now().minusDays(1));
        peerCodeRepository.save(expiredPeerCode);

        PeerCode validPeerCode = PeerCode.create(stubUser("peer-valid"), 7L);
        peerCodeRepository.save(validPeerCode);

        AssessmentToken expiredToken = AssessmentToken.create(stubUser("token-expired"), null, 7);
        setField(expiredToken, "expiresAt", LocalDateTime.now().minusDays(1));
        assessmentTokenRepository.save(expiredToken);

        AssessmentToken validToken = AssessmentToken.create(stubUser("token-valid"), null, 7);
        assessmentTokenRepository.save(validToken);

        // when
        scheduler.cleanup();

        // then
        assertThat(peerCodeRepository.findById(expiredPeerCode.getId())).isEmpty();
        assertThat(peerCodeRepository.findById(validPeerCode.getId()).isPresent());
        assertThat(assessmentTokenRepository.findById(expiredToken.getId())).isEmpty();
        assertThat(assessmentTokenRepository.findById(validToken.getId())).isPresent();
    }
}