package com.mycpt.backend.domain.user.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.common.storage.StorageService;
import com.mycpt.backend.domain.assessment.repository.AssessmentTokenRepository;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.coin.repository.CoinTransactionRepository;
import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.colleague.repository.ColleagueRepository;
import com.mycpt.backend.domain.colleague.repository.PeerCodeRepository;
import com.mycpt.backend.domain.notification.repository.NotificationRepository;
import com.mycpt.backend.domain.notification.service.NotificationService;
import com.mycpt.backend.domain.result.repository.DiscTestRepository;
import com.mycpt.backend.domain.user.client.KakaoUnlinkClient;
import com.mycpt.backend.domain.user.dto.UpdateProfileRequest;
import com.mycpt.backend.domain.user.dto.WithdrawRequest;
import com.mycpt.backend.domain.user.dto.WithdrawalInfoResponse;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.enums.Gender;
import com.mycpt.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static com.mycpt.backend.support.EntityTestSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StorageService storageService;
    @Mock private DiscTestRepository discTestRepository;
    @Mock private CoinTransactionRepository coinTransactionRepository;
    @Mock private PeerCodeRepository peerCodeRepository;
    @Mock private AssessmentTokenRepository assessmentTokenRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationService notificationService;
    @Mock private ColleagueRepository colleagueRepository;
    @Mock private ChemistryReportRepository chemistryReportRepository;
    @Mock private KakaoUnlinkClient kakaoUnlinkClient;

    private UserService sut() {
        return new UserService(
                userRepository, storageService, discTestRepository, coinTransactionRepository,
                peerCodeRepository, assessmentTokenRepository, notificationRepository,
                notificationService, colleagueRepository, chemistryReportRepository, kakaoUnlinkClient
        );
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private User stubUser() {
        return User.create("kakao-1", "기존닉네임", "https://example.com/old.jpg");
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        @Test
        @DisplayName("[UT-UserSvc-프로필수정-성공]")
        void 프로필수정_성공() {
            // given
            User user = stubUser();
            given(userRepository.getReferenceById(1L)).willReturn(user);
            given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            UpdateProfileRequest request =
                    new UpdateProfileRequest("새닉네임", 1998, "M");

            // when
            User result = sut().updateProfile(1L, request);

            // then
            assertThat(result.getNickname()).isEqualTo("새닉네임");
            assertThat(result.getBirthYear()).isEqualTo(1998);
            assertThat(result.getGender()).isEqualTo(Gender.M);
        }

        @Test
        @DisplayName("[UT-UserSvc-프로필수정-부분수정]")
        void 프로필수정_부분수정() {
            // given: nickname만 포함, birthYear/gender는 null
            User user = stubUser();
            // stubUser() 메서드에는 birthYear/gender가 null이므로 부분 수정 후에도 null 유지됨을 검증
            given(userRepository.getReferenceById(1L)).willReturn(user);
            given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            UpdateProfileRequest request =
                    new UpdateProfileRequest("새닉네임", null, null);

            // when
            User result = sut().updateProfile(1L, request);

            // then
            assertThat(result.getNickname()).isEqualTo("새닉네임");
            assertThat(result.getBirthYear()).isNull(); // 기존 값 유지
            assertThat(result.getGender()).isNull();    // 기존 값 유지
        }
    }

    // ── updateProfileImage ────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProfileImage()")
    class UpdateProfileImage {

        @Test
        @DisplayName("[UT-UserSvc-이미지업로드-형식오류]")
        void 이미지업로드_형식오류() {
            // given: text/plain 파일
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.txt", "text/plain", "content".getBytes()
            );

            // when
            assertThatThrownBy(() -> sut().updateProfileImage(1L, file))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(be.getMessage()).contains("jpg, png, webp");
                    });
        }

        @Test
        @DisplayName("[UT-UserSvc-이미지업로드-크기초과]")
        void 이미지업로드_크기초과() {
            // given: 11MB (10MB 초과)
            byte[] content = new byte[11 * 1024 * 1024];
            MockMultipartFile file = new MockMultipartFile(
                    "image", "big.jpg", "image/jpeg", content
            );

            // when
            assertThatThrownBy(() -> sut().updateProfileImage(1L, file))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                        assertThat(be.getMessage()).contains("10MB");
                    });
        }
    }

    // ── withdraw() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("withdraw()")
    class Withdraw {

        private User stubUser(Long id, String kakaoId) {
            User user = User.create(kakaoId, "유신", null);
            return setId(user, id);
        }

        private Colleague stubColleague(User userA, User userB) {
            return Colleague.create(userA, userB);
        }

        @Test
        @DisplayName("[UT-UserSvc-탈퇴-성공]")
        void 탈퇴_성공() {
            // given
            User me = stubUser(1L, "kakao-1");
            given(userRepository.getReferenceById(1L)).willReturn(me);
            given(colleagueRepository.findAllByUserId(1L)).willReturn(List.of());
            given(discTestRepository.findAllByUserId(1L)).willReturn(List.of());
            given(notificationRepository.findAllByUserId(1L)).willReturn(List.of());

            // when
            sut().withdraw(1L, new WithdrawRequest("필요 없어짐"));

            // then
            then(coinTransactionRepository).should(times(1)).deleteByUserId(1L);
            then(peerCodeRepository).should(times(1)).deleteByUserId(1L);
            then(assessmentTokenRepository).should(times(1)).deleteBySubjectId(1L);
            then(kakaoUnlinkClient).should(times(1)).unlink("kakao-1");
            then(userRepository).should(times(1)).save(argThat(u ->
                    u.getKakaoId() == null && u.getNickname().equals("유신")
            ));
        }

        @Test
        @DisplayName("[UT-UserSvc-탈퇴-동료관계전체삭제]")
        void 탈퇴_동료관계전체삭제() {
            // given
            User me = stubUser(1L, "kakao-1");
            Colleague c1 = stubColleague(me, stubUser(2L, "kakao-2"));
            Colleague c2 = stubColleague(me, stubUser(3L, "kakao-3"));
            List<Colleague> colleagues = List.of(c1, c2);

            given(userRepository.getReferenceById(1L)).willReturn(me);
            given(colleagueRepository.findAllByUserId(1L)).willReturn(colleagues);
            given(discTestRepository.findAllByUserId(1L)).willReturn(List.of());
            given(notificationRepository.findAllByUserId(1L)).willReturn(List.of());

            // when
            sut().withdraw(1L, null);

            // then
            then(notificationService).should(times(1)).deleteColleagueNotifications(c1);
            then(notificationService).should(times(1)).deleteColleagueNotifications(c2);
            then(colleagueRepository).should(times(1)).deleteAll(colleagues);
        }

        @Test
        @DisplayName("[UT-UserSvc-탈퇴-카카오ID이미없음]")
        void 탈퇴_카카오ID이미없음() {
            // given
            User me = stubUser(1L, "kakao-1");
            me.withdraw();  // kakaoId를 미리 null로 만든 방어적 시나리오
            given(userRepository.getReferenceById(1L)).willReturn(me);
            given(colleagueRepository.findAllByUserId(1L)).willReturn(List.of());
            given(discTestRepository.findAllByUserId(1L)).willReturn(List.of());
            given(notificationRepository.findAllByUserId(1L)).willReturn(List.of());

            // when
            sut().withdraw(1L, null);

            // then
            then(kakaoUnlinkClient).should(never()).unlink(any());
        }

        @Test
        @DisplayName("[UT-UserSvc-탈퇴-카카오unlink실패]")
        void 탈퇴_카카오unlink실패() {
            // given
            User me = stubUser(1L, "kakao-1");
            given(userRepository.getReferenceById(1L)).willReturn(me);
            given(colleagueRepository.findAllByUserId(1L)).willReturn(List.of());
            given(discTestRepository.findAllByUserId(1L)).willReturn(List.of());
            given(notificationRepository.findAllByUserId(1L)).willReturn(List.of());
            willThrow(new RuntimeException("카카오 API 오류"))
                    .given(kakaoUnlinkClient).unlink("kakao-1");

            // when
            assertThatThrownBy(() -> sut().withdraw(1L, null))
                    // then
                    .isInstanceOf(RuntimeException.class);
            then(userRepository).should(never()).save(any());
        }
    }

    // ── getWithdrawalInfo() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getWithdrawalInfo()")
    class GetWithdrawalInfo {

        @Test
        @DisplayName("[UT-UserSvc-탈퇴전카운트조회-성공]")
        void 탈퇴전카운트조회_성공() {
            // given
            User me = User.create("kakao-1", "유신", null);
            User partner = User.create("kakao-2", "민준", null);
            given(discTestRepository.countByUserId(1L)).willReturn(5L);
            given(chemistryReportRepository.countByUserId(1L)).willReturn(4L);
            given(colleagueRepository.countByUserId(1L)).willReturn(1L);

            // when
            WithdrawalInfoResponse result = sut().getWithdrawalInfo(1L);

            // then
            assertThat(result.resultCount()).isEqualTo(5L);
            assertThat(result.chemistryCount()).isEqualTo(4L);
            assertThat(result.colleagueCount()).isEqualTo(1L);
        }
    }
}