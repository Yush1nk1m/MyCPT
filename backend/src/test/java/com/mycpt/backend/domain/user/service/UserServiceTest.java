package com.mycpt.backend.domain.user.service;

import com.mycpt.backend.common.storage.StorageService;
import com.mycpt.backend.domain.user.dto.UpdateProfileRequest;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StorageService storageService;

    private UserService sut() {
        return new UserService(userRepository, storageService);
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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("jpg, png, webp");
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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("10MB");
        }
    }
}