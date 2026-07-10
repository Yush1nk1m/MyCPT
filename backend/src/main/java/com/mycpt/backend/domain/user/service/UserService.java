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
import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 회원 프로필 서비스
 *
 * updateProfile()      - PATCH /users/me (닉네임, 생년, 성별)
 * updateProfileImage() - POST /users/me/profile-image (이미지 업로드 + URL 교체)
 */
@Slf4j
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
    private final DiscTestRepository discTestRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final PeerCodeRepository peerCodeRepository;
    private final AssessmentTokenRepository assessmentTokenRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ColleagueRepository colleagueRepository;
    private final ChemistryReportRepository chemistryReportRepository;
    private final KakaoUnlinkClient kakaoUnlinkClient;

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
     * GET /users/me/withdrawal-info - 탈퇴 Step 1 삭제 항목 카운트
     */
    @Transactional(readOnly = true)
    public WithdrawalInfoResponse getWithdrawalInfo(Long userId) {
        long resultCount = discTestRepository.countByUserId(userId);
        long chemistryCount = chemistryReportRepository.countByUserId(userId);
        long colleagueCount = colleagueRepository.countByUserId(userId);

        return new WithdrawalInfoResponse(resultCount, chemistryCount, colleagueCount);
    }

    /**
     * DELETE /users/me — 회원 탈퇴
     * <p>
     * 삭제 정책 (service-design.md §회원 탈퇴 정책):
     * - 하드 삭제: tests(+disc_tests), coin_transactions, peer_codes, assessment_tokens(subject),
     * notifications, colleagues
     * - 유지:      chemistry_reports (상대방이 이전 보고서를 계속 열람해야 함)
     * - 익명화:    users 행 자체는 삭제하지 않고 kakao_id/birth_year/gender만 NULL 처리
     */
    @Transactional
    public void withdraw(Long userId, WithdrawRequest request) {
        User user = userRepository.getReferenceById(userId);
        log.info("회원 탈퇴 시작. userId={}, reason={}", userId, request != null ? request.reason() : null);

        // TODO: 추후 성능 문제 시 벌크 삭제로 변경 필요

        // 1. 동료 관계 전체 삭제 - 기존 ColleagueService.delete()와 동일 순서(알림 정리 -> 관계 삭제)
        List<Colleague> colleagues = colleagueRepository.findAllByUserId(userId);
        for (Colleague colleague : colleagues) {
            notificationService.deleteColleagueNotifications(colleague);
        }
        colleagueRepository.deleteAll(colleagues);

        // 2. 본인 소유 데이터 하드 삭제
        //    tests/notifications는 JOINED 상속이므로 엔티티 기반 deleteAll() 사용
        //    - 벌크 JPQL DELETE는 자식 테이블만 지워 부모 행이 고아로 남음
        discTestRepository.deleteAll(discTestRepository.findAllByUserId(userId));
        coinTransactionRepository.deleteByUserId(userId);
        peerCodeRepository.deleteByUserId(userId);
        assessmentTokenRepository.deleteBySubjectId(userId);
        notificationRepository.deleteAll(notificationRepository.findAllByUserId(userId));

        // 3. 카카오 연결 해제 - kakaoId를 지우기 전에 호출해야 함
        if (user.getKakaoId() != null) {
            kakaoUnlinkClient.unlink(user.getKakaoId());
        }

        // 4. users 행 익명화
        user.withdraw();
        userRepository.save(user);

        log.info("회원 탈퇴 완료. userId={}", userId);
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
