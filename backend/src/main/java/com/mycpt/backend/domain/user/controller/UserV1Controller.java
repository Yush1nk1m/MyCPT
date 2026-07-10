package com.mycpt.backend.domain.user.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.user.dto.*;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserV1Controller implements UserApi {

    private final UserService userService;

    // PATCH /api/v1/users/me
    @PatchMapping("/me")
    @Override
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request
    ) {
        User updatedUser = userService.updateProfile(principal.getUser().getId(), request);
        return ResponseEntity.ok(UpdateProfileResponse.from(updatedUser));
    }

    // POST /api/v1/users/me/profile-image
    @PostMapping("/me/profile-image")
    @Override
    public ResponseEntity<UpdateProfileImageResponse> updateProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("image") MultipartFile image
    ) {
        String url = userService.updateProfileImage(principal.getUser().getId(), image);
        return ResponseEntity.ok(new UpdateProfileImageResponse(url));
    }

    // GET /api/v1/users/me/withdrawal-info
    @GetMapping("/me/withdrawal-info")
    @Override
    public ResponseEntity<WithdrawalInfoResponse> getWithdrawalInfo(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(userService.getWithdrawalInfo(principal.getUser().getId()));
    }

    // DELETE /api/v1/users/me
    @DeleteMapping("/me")
    @Override
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false)WithdrawRequest request,
            HttpServletResponse response
    ) {
        userService.withdraw(principal.getUser().getId(), request);

        // TODO: 추후 로그아웃 로직을 호출하도록 수정
        // SecurityConfig의 logoutSuccessHandler와 동일한 방식으로 JWT 쿠키 무효화
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
