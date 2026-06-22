package com.mycpt.backend.domain.notification.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.notification.dto.NotificationListResponse;
import com.mycpt.backend.domain.notification.dto.NotificationResponse;
import com.mycpt.backend.domain.notification.entity.ColleagueNotification;
import com.mycpt.backend.domain.notification.entity.Notification;
import com.mycpt.backend.domain.notification.repository.NotificationRepository;
import com.mycpt.backend.domain.user.entity.User;
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
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService sut() {
        return new NotificationService(notificationRepository);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private User stubUser(Long id) {
        return setId(User.create("kakao-" + id, "닉네임" + id, null), id);
    }

    private ColleagueNotification stubColleagueNotification(User recipient, User requester) {
        Colleague colleague = Colleague.create(recipient, requester);
        return ColleagueNotification.create(recipient, colleague);
    }

    // ── sendColleagueNotification() ───────────────────────────────────────────

    @Nested
    @DisplayName("sendColleagueNotification()")
    class SendColleagueNotification {

        @Test
        @DisplayName("[UT-NotificationSvc-동료알림전송-성공]")
        void 동료알림전송_성공() {
            // given
            User recipient = stubUser(1L);
            User requester = stubUser(2L);
            Colleague colleague = Colleague.create(recipient, requester);

            given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            sut().sendColleagueNotification(recipient, colleague);

            // then
            then(notificationRepository).should(times(1)).save(any(ColleagueNotification.class));
        }
    }

    // ── list() ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("list()")
    class List_ {

        @Test
        @DisplayName("[UT-NotificationSvc-알림목록조회-성공]")
        void 알림목록조회_성공() {
            // given
            User recipient = stubUser(1L);
            User requester = stubUser(2L);

            Colleague colleague = setId(Colleague.create(recipient, requester), 5L);
            ColleagueNotification n1 = setId(ColleagueNotification.create(recipient, colleague), 10L);

            given(notificationRepository.findAllByUserId(1L))
                    .willReturn(List.of(n1));

            // when
            NotificationListResponse response = sut().list(1L);

            // then
            assertThat(response.notifications()).hasSize(1);
            NotificationResponse item = response.notifications().get(0);
            assertThat(item.notificationId()).isEqualTo(10L);
            assertThat(item.type()).isEqualTo("COLLEAGUE_REGISTERED");
            assertThat(item.referenceId()).isEqualTo(5L);
        }
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("[UT-NotificationSvc-알림삭제-성공]")
        void 알림삭제_성공() {
            // given
            User recipient = stubUser(1L);
            User requester = stubUser(2L);
            Notification notification = setId(
                    stubColleagueNotification(recipient, requester), 10L
            );

            given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

            // when
            sut().delete(10L, 1L);

            // then
            then(notificationRepository).should(times(1)).delete(any(Notification.class));
        }

        @Test
        @DisplayName("[UT-NotificationSvc-알림삭제-없는ID]")
        void 알림삭제_없는ID() {
            // given
            given(notificationRepository.findById(any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> sut().delete(999L, 1L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("[UT-NotificationSvc-알림삭제-권한없음]")
        void 알림삭제_권한없음() {
            // given: 알림 수신자 userId=1, 요청자 userId=99
            User recipient = stubUser(1L);
            User requester = stubUser(2L);
            Notification notification = setId(
                    stubColleagueNotification(recipient, requester), 10L
            );

            given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

            // when
            assertThatThrownBy(() -> sut().delete(10L, 99L))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    });
            then(notificationRepository).should(never()).delete(any());
        }
    }
}