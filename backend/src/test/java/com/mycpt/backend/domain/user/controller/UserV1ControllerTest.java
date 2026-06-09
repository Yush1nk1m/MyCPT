package com.mycpt.backend.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycpt.backend.domain.user.dto.UpdateProfileRequest;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.enums.Gender;
import com.mycpt.backend.domain.user.service.UserService;
import com.mycpt.backend.support.SliceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserV1Controller.class)
@DisplayName("UserV1Controller 슬라이스 테스트")
class UserV1ControllerTest extends SliceTestSupport {

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("PATCH /api/v1/users/me")
    class UpdateProfile {

        @Test
        @DisplayName("[ST-UserCtrl-프로필수정-성공]")
        void 프로필수정_성공() throws Exception {
            // given
            User user = testUser();
            // updateProfile() 메서드 호출 결과로 수정된 User 반환 시뮬레이션
            // testUser()는 birthYear/gender 필드가 null이므로 직접 업데이트 메서드 호출
            user.updateProfile("새닉네임", 1998, Gender.M);
            given(userService.updateProfile(any(), any())).willReturn(user);

            UpdateProfileRequest request =
                    new UpdateProfileRequest("새닉네임", 1998, "M");

            // when
            mockMvc.perform(patch("/api/v1/users/me")
                            .with(authenticated(testUser()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nickname").value("새닉네임"))
                    .andExpect(jsonPath("$.birthYear").value(1998))
                    .andExpect(jsonPath("$.gender").value("M"));
        }

        @Test
        @DisplayName("[ST-UserCtrl-프로필수정-미인증]")
        void 프로필수정_미인증() throws Exception {
            // given: 쿠키 없이 요청
            UpdateProfileRequest request =
                    new UpdateProfileRequest("새닉네임", null, null);

            // when
            mockMvc.perform(patch("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }
}