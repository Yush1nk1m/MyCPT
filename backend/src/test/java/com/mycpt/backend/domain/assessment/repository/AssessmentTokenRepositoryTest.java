package com.mycpt.backend.domain.assessment.repository;

import com.mycpt.backend.domain.assessment.entity.AssessmentToken;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.JpaTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AssessmentTokenRepository 슬라이스 테스트")
class AssessmentTokenRepositoryTest extends JpaTestSupport {

    @Autowired private AssessmentTokenRepository assessmentTokenRepository;
    @Autowired private UserRepository userRepository;

    @Nested
    @DisplayName("deleteBySubjectId()")
    class DeleteBySubjectId {

        @Test
        @DisplayName("[ST-AssessmentTokenRepo-대상자별삭제-성공]")
        void 대상자별삭제_성공() {
            // given
            User subject = userRepository.save(User.create("kakao-a", "A", null));
            AssessmentToken token = AssessmentToken.create(subject, "친구", 7);
            assessmentTokenRepository.save(token);

            // when
            assessmentTokenRepository.deleteBySubjectId(subject.getId());

            // then
            assertThat(assessmentTokenRepository.findByToken(token.getToken())).isEmpty();
        }
    }

}