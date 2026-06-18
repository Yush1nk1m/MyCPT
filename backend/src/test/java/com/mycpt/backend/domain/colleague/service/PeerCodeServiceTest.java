package com.mycpt.backend.domain.colleague.service;

import com.mycpt.backend.domain.colleague.dto.PeerCodeResponse;
import com.mycpt.backend.domain.colleague.entity.PeerCode;
import com.mycpt.backend.domain.colleague.repository.PeerCodeRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PeerCodeService 단위 테스트")
class PeerCodeServiceTest {

    @Mock
    private PeerCodeRepository peerCodeRepository;
    @Mock
    private UserRepository userRepository;

    private PeerCodeService sut() {
        return new PeerCodeService(peerCodeRepository, userRepository);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private User stubUser() {
        return User.create("kakao-1", "닉네임", null);
    }

    // 유효한 PeerCode (만료되지 않음)
    private PeerCode stubValidCode(User user) {
        return PeerCode.create(user, 7L);
    }

    // 만료된 PeerCode (ttlDays=0으로 생성 -> 즉시 만료)
    private PeerCode stubExpiredCode(User user) {
        PeerCode pc = PeerCode.create(user, -1L);
        return pc;
    }

    // ── getOrCreate() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getOrCreate()")
    class GetOrCreate {

        @Test
        @DisplayName("[UT-PeerCodeSvc-코드조회-행없음]")
        void 코드조회_행없음() {
            // given
            given(peerCodeRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(userRepository.getReferenceById(1L)).willReturn(stubUser());
            given(peerCodeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            PeerCodeResponse response = sut().getOrCreate(1L);

            // then
            then(peerCodeRepository).should(times(1)).save(any(PeerCode.class));
            assertThat(response.code()).isNotNull();
            assertThat(response.expiresAt()).isNotNull();
        }

        @Test
        @DisplayName("[UT-PeerCodeSvc-코드조회-유효]")
        void 코드조회_유효() {
            // given
            User user = stubUser();
            PeerCode existing = stubValidCode(user);
            String originalCode = existing.getCode();

            given(peerCodeRepository.findByUserId(1L)).willReturn(Optional.of(existing));

            // when
            PeerCodeResponse response = sut().getOrCreate(1L);

            // then: save 미호출, 기존 코드 그대로 반환
            then(peerCodeRepository).should(never()).save(any());
            assertThat(response.code()).isEqualTo(originalCode);
        }

        @Test
        @DisplayName("[UT-PeerCodeSvc-코드조회-만료]")
        void 코드조회_만료() {
            // given
            User user = stubUser();
            PeerCode expired = stubExpiredCode(user);
            String originalCode = expired.getCode();

            given(peerCodeRepository.findByUserId(1L)).willReturn(Optional.of(expired));
            given(peerCodeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            PeerCodeResponse response = sut().getOrCreate(1L);

            // then: refresh 후 save 1회, 새 코드 반환
            then(peerCodeRepository).should(times(1)).save(any(PeerCode.class));
            assertThat(response.code()).isNotEqualTo(originalCode);
        }
    }

    // ── refresh() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("[UT-PeerCodeSvc-코드갱신-성공]")
        void 코드갱신_성공() {
            // given
            User user = stubUser();
            PeerCode existing = stubValidCode(user);
            String originalCode = existing.getCode();

            given(peerCodeRepository.findByUserId(1L)).willReturn(Optional.of(existing));
            given(peerCodeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            PeerCodeResponse response = sut().refresh(1L);

            // then
            then(peerCodeRepository).should(times(1)).save(any(PeerCode.class));
            assertThat(response.code()).isNotEqualTo(originalCode);
        }

        @Test
        @DisplayName("[UT-PeerCodeSvc-코드갱신-행없음]")
        void 코드갱신_행없음() {
            // given: 행 없으면 신규 생성 후 refresh (방어 로직)
            given(peerCodeRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(userRepository.getReferenceById(1L)).willReturn(stubUser());
            given(peerCodeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            PeerCodeResponse response = sut().refresh(1L);

            // then
            then(peerCodeRepository).should(times(1)).save(any(PeerCode.class));
            assertThat(response.code()).isNotNull();
        }
    }
}