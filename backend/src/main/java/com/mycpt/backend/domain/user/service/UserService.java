package com.mycpt.backend.domain.user.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.common.storage.StorageService;
import com.mycpt.backend.domain.user.dto.UpdateProfileRequest;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 회원 프로필 서비스
 *
 * updateProfile()      - PATCH /users/me (닉네임, 생년, 성별)
 * updateProfileImage() - POST /users/me/profile-image (이미지 업로드 + URL 교체)
 */
@Service
@RequiredArgsConstructor
public class UserService {

    // 허용 이미지 확장자
    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");
    // 최대 파일 크기: 10MB
    private static final long MAX_SIZE = 10L * 1024 * 1024;

    private final UserRepository userRepository;
    private final StorageService storageService;

    /**
     * 프로필 정보 수정 - null 필드는 기존 값 유지
     */
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.getReferenceById(userId);
        user.updateProfile(request.nickname(), request.birthYear(), request.genderEnum());

        // @Transactional dirty checking으로 자동 UPDATE
        // AssessmentService 패턴과의 일관성 유지를 위해 명시적으로 save() 메서드 호출
        return userRepository.save(user);
    }

    /**
     * 프로필 이미지 업로드 및 URL 교체
     * 검증 -> 스토리지 저장 -> users.profile_image_url UPDATE
     */
    @Transactional
    public String updateProfileImage(Long userId, MultipartFile file) {
        validateImage(file);

        String url = storageService.store(file, "profiles");

        User user = userRepository.getReferenceById(userId);
        user.updateProfileImageUrl(url);
        userRepository.save(user);

        return url;
    }

    // ── private ───────────────────────────────────────────

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "파일이 없습니다.");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "jpg, png, webp 형식만 업로드할 수 있습니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }
}
