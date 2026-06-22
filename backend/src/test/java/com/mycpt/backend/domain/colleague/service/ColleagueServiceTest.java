package com.mycpt.backend.domain.colleague.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.colleague.dto.ColleagueListResponse;
import com.mycpt.backend.domain.colleague.dto.ColleagueResponse;
import com.mycpt.backend.domain.colleague.dto.InviteInfoResponse;
import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.colleague.entity.PeerCode;
import com.mycpt.backend.domain.colleague.repository.ColleagueRepository;
import com.mycpt.backend.domain.colleague.repository.PeerCodeRepository;
import com.mycpt.backend.domain.notification.service.NotificationService;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ColleagueService 단위 테스트")
class ColleagueServiceTest {

    @Mock
    private ColleagueRepository colleagueRepository;
    @Mock
    private PeerCodeRepository peerCodeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    private ColleagueService sut() {
        return new ColleagueService(colleagueRepository, peerCodeRepository, userRepository, notificationService);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private User stubUser(Long id, String kakaoId) {
        User user = User.create(kakaoId, "닉네임" + id, null);
        return setId(user, id);
    }

    private PeerCode stubValidCode(User user) {
        return PeerCode.create(user, 7L);
    }

    private PeerCode stubExpiredCode(User user) {
        return PeerCode.create(user, -1L);
    }

    private Colleague stubColleague(User userA, User userB) {
        return Colleague.create(userA, userB);
    }

    // ── getInviteInfo() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getInviteInfo()")
    class GetInviteInfo {

        @Test
        @DisplayName("[UT-ColleagueSvc-초대정보조회-성공]")
        void 초대정보조회_성공() {
            // given
            User inviter = stubUser(1L, "kakao-1");
            PeerCode code = stubValidCode(inviter);

            given(peerCodeRepository.findByCode("ABCD1234"))
                    .willReturn(Optional.of(code));

            // when
            InviteInfoResponse response = sut().getInviteInfo("ABCD1234", 2L);

            // then
            assertThat(response.nickname()).isEqualTo(inviter.getNickname());
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-초대정보조회-코드없음]")
        void 초대정보조회_코드없음() {
            // given
            given(peerCodeRepository.findByCode(any()))
                    .willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> sut().getInviteInfo("INVALID1", 2L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-초대정보조회-만료코드]")
        void 초대정보조회_만료코드() {
            // given
            User inviter = stubUser(1L, "kakao-1");
            PeerCode expired = stubExpiredCode(inviter);

            given(peerCodeRepository.findByCode(any()))
                    .willReturn(Optional.of(expired));

            // when
            assertThatThrownBy(() -> sut().getInviteInfo("EXPIRED1", 2L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_CODE);
                    });
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-초대정보조회-자기초대]")
        void 초대정보조회_자기초대() {
            // given
            User inviter = stubUser(1L, "kakao-1");
            PeerCode code = stubValidCode(inviter);

            given(peerCodeRepository.findByCode(any()))
                    .willReturn(Optional.of(code));

            // when
            assertThatThrownBy(() -> sut().getInviteInfo(code.getCode(), inviter.getId()))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.SELF_INVITE);
                    });
        }
    }

    // ── register() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("[UT-ColleagueSvc-동료등록-성공]")
        void 동료등록_성공() {
            // given
            User inviter = stubUser(1L, "kakao-1");
            User requester = stubUser(2L, "kakao-2");
            PeerCode code = stubValidCode(inviter);

            given(peerCodeRepository.findByCode(any())).willReturn(Optional.of(code));
            given(userRepository.getReferenceById(any())).willReturn(requester);
            given(colleagueRepository.existsByPair(any(), any())).willReturn(false);
            given(colleagueRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            ColleagueResponse response = sut().register(code.getCode(), requester.getId());

            // then
            then(colleagueRepository).should(times(1)).save(any(Colleague.class));
            then(notificationService).should(times(1))
                    .sendColleagueNotification(any(User.class), any(Colleague.class));
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-동료등록-코드없음]")
        void 동료등록_코드없음() {
            // given
            given(peerCodeRepository.findByCode(any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> sut().register("INVALID1", 2L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-동료등록-만료코드]")
        void 동료등록_만료코드() {
            // given
            User inviter = stubUser(1L, "kakao-1");
            PeerCode expired = stubExpiredCode(inviter);

            given(peerCodeRepository.findByCode(any())).willReturn(Optional.of(expired));

            // when
            assertThatThrownBy(() -> sut().register("EXPIRED1", 2L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_CODE);
                    });
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-동료등록-자기초대]")
        void 동료등록_자기초대() {
            // given
            User inviter = stubUser(1L, "kakao-1");
            PeerCode code = stubValidCode(inviter);

            given(peerCodeRepository.findByCode(any())).willReturn(Optional.of(code));

            // when
            assertThatThrownBy(() -> sut().register(code.getCode(), inviter.getId()))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.SELF_INVITE);
                    });
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-동료등록-이미동료]")
        void 동료등록_이미동료() {
            // given
            User inviter = stubUser(1L, "kakao-1");
            User requester = stubUser(2L, "kakao-2");
            PeerCode code = stubValidCode(inviter);

            given(peerCodeRepository.findByCode(any())).willReturn(Optional.of(code));
            given(userRepository.getReferenceById(any())).willReturn(requester);
            given(colleagueRepository.existsByPair(any(), any())).willReturn(true);

            // when
            assertThatThrownBy(() -> sut().register(code.getCode(), requester.getId()))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ALREADY_COLLEAGUE);
                    });
            then(colleagueRepository).should(never()).save(any());
        }
    }

    // ── list() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("list()")
    class List_ {

        @Test
        @DisplayName("[UT-ColleagueSvc-동료목록조회-성공]")
        void 동료목록조회_성공() {
            // given
            User me = stubUser(1L, "kakao-1");
            User partner = stubUser(2L, "kakao-2");
            Colleague colleague = stubColleague(me, partner);

            given(colleagueRepository.findAllByUserId(any()))
                    .willReturn(List.of(colleague));

            // when
            ColleagueListResponse response = sut().list(me.getId());

            // then
            assertThat(response.colleagues()).hasSize(1);
        }
    }

    // ── get() ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("get()")
    class Get {

        @Test
        @DisplayName("[UT-ColleagueSvc-동료프로필조회-성공]")
        void 동료프로필조회_성공() {
            // given
            User me = stubUser(1L, "kakao-1");
            User partner = stubUser(2L, "kakao-2");
            Colleague colleague = stubColleague(me, partner);

            given(userRepository.findById(any())).willReturn(Optional.of(partner));
            given(colleagueRepository.findByPair(any(), any())).willReturn(Optional.of(colleague));

            // when
            ColleagueResponse response = sut().get(partner.getId(), me.getId());

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-동료프로필조회-동료아님]")
        void 동료프로필조회_동료아님() {
            // given
            given(userRepository.findById(any())).willReturn(Optional.of(stubUser(2L, "kakao-2")));
            given(colleagueRepository.findByPair(any(), any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> sut().get(2L, 1L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    });
        }
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("[UT-ColleagueSvc-동료삭제-성공]")
        void 동료삭제_성공() {
            // given
            User me = stubUser(1L, "kakao-1");
            User partner = stubUser(2L, "kakao-2");
            Colleague colleague = stubColleague(me, partner);

            given(userRepository.findById(any())).willReturn(Optional.of(partner));
            given(colleagueRepository.findByPair(any(), any())).willReturn(Optional.of(colleague));

            // when
            sut().delete(partner.getId(), me.getId());

            // then
            then(colleagueRepository).should(times(1)).delete(any(Colleague.class));
        }

        @Test
        @DisplayName("[UT-ColleagueSvc-동료삭제-동료아님]")
        void 동료삭제_동료아님() {
            // given
            given(userRepository.findById(any())).willReturn(Optional.of(stubUser(2L, "kakao-2")));
            given(colleagueRepository.findByPair(any(), any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> sut().delete(2L, 1L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    });
            then(colleagueRepository).should(never()).delete(any());
        }
    }
}