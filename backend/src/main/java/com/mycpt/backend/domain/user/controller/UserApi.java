package com.mycpt.backend.domain.user.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.user.dto.UpdateProfileImageResponse;
import com.mycpt.backend.domain.user.dto.UpdateProfileRequest;
import com.mycpt.backend.domain.user.dto.UpdateProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원", description = "회원 프로필 수정 API")
public interface UserApi {

    @Operation(
            summary = "프로필 정보 수정",
            description = "닉네임, 생년, 성별을 수정한다. 요청에 포함된 필드만 업데이트한다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    ResponseEntity<UpdateProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request
    );

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "jpg/png/webp, 10MB 이하. 기존 이미지를 새 URL로 교체한다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    ResponseEntity<UpdateProfileImageResponse> updateProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            MultipartFile image
    );
}
