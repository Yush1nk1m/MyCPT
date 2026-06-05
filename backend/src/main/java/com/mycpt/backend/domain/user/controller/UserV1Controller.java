package com.mycpt.backend.domain.user.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.user.dto.UpdateProfileRequest;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserV1Controller implements UserApi {

    private final UserService userService;

    // PATCH /api/v1/users/me
    @PatchMapping("/me")
    @Override
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request
    ) {
        User updated = userService.updateProfile(principal.getUser().getId(), request);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nickname", updated.getNickname());
        body.put("birthYear", updated.getBirthYear());
        body.put("gender", updated.getGender() != null ? updated.getGender().name() : null);
        return ResponseEntity.ok(body);
    }

    // POST /api/v1/users/me/profile-image
    @PostMapping("/me/profile-image")
    @Override
    public ResponseEntity<Map<String, String>> updateProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("image") MultipartFile image
    ) {
        String url = userService.updateProfileImage(principal.getUser().getId(), image);
        return ResponseEntity.ok(Map.of("profileImageUrl", url));
    }
}
