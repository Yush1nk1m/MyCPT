package com.mycpt.backend.domain.colleague.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.colleague.dto.ColleagueListResponse;
import com.mycpt.backend.domain.colleague.dto.ColleagueResponse;
import com.mycpt.backend.domain.colleague.dto.InviteInfoResponse;
import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.colleague.repository.ColleagueRepository;
import com.mycpt.backend.domain.colleague.repository.PeerCodeRepository;
import com.mycpt.backend.domain.notification.service.NotificationService;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColleagueService {

    private final ColleagueRepository colleagueRepository;
    private final PeerCodeRepository peerCodeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * GET /colleagues/invite/{code} - 초대 코드로 초대자 정보 조회
     *
     * 동료 등록 전 초대자 프로필 미리보기용
     * 만료 코드도 여기서 검증 (POST와 동일한 유효성 검사)
     */
    @Transactional(readOnly = true)
    public InviteInfoResponse getInviteInfo(String code, Long myUserId) {
        var peerCode = peerCodeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (peerCode.isExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_CODE);
        }

        User inviter = peerCode.getUser();

        if (inviter.getId().equals(myUserId)) {
            throw new BusinessException(ErrorCode.SELF_INVITE);
        }

        return new InviteInfoResponse(
                inviter.getId(),
                inviter.getNickname(),
                inviter.getProfileImageUrl()
        );
    }

    /**
     * POST /colleagues - 동료 등록
     * <p>
     * 1. 코드 유효성 검증
     * 2. SELF_INVITE / ALREADY_COLLEAGUE 검증
     * 3. colleagues INSERT (user_a_id < user_b_id 정렬)
     * 4. 초대자에게 알림 전송
     */
    @Transactional
    public ColleagueResponse register(String code, Long myUserId) {
        var peerCode = peerCodeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (peerCode.isExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_CODE);
        }

        User inviter = peerCode.getUser();
        User requester = userRepository.getReferenceById(myUserId);

        if (inviter.getId().equals(myUserId)) {
            throw new BusinessException(ErrorCode.SELF_INVITE);
        }

        // user_a_id < user_b_id 정렬
        Long idA = Math.min(inviter.getId(), myUserId);
        Long idB = Math.max(inviter.getId(), myUserId);

        if (colleagueRepository.existsByPair(idA, idB)) {
            throw new BusinessException(ErrorCode.ALREADY_COLLEAGUE);
        }

        User userA = idA.equals(inviter.getId()) ? inviter : requester;
        User userB = idB.equals(inviter.getId()) ? inviter : requester;

        Colleague colleague = Colleague.create(userA, userB);
        colleagueRepository.save(colleague);

        // 초대자에게 알림 전송 (동료 등록 완료)
        notificationService.sendColleagueNotification(inviter, colleague);

        return ColleagueResponse.from(colleague, myUserId);
    }

    /**
     * GET /colleagues - 동료 목록 조회
     */
    @Transactional(readOnly = true)
    public ColleagueListResponse list(Long myUserId) {
        List<ColleagueResponse> result = colleagueRepository.findAllByUserId(myUserId)
                .stream()
                .map(c -> ColleagueResponse.from(c, myUserId))
                .toList();

        return new ColleagueListResponse(result);
    }

    /**
     * GET /colleagues/{colleagueId} - 동료 프로필 조회
     * <p>
     * colleagueId는 colleagues.id가 아닌 상대방의 userId
     * (API 명세: colleagueId = 조회할 동료의 userId)
     */
    @Transactional(readOnly = true)
    public ColleagueResponse get(Long colleagueUserId, Long myUserId) {
        // 상대방이 존재하는지 확인
        userRepository.findById(colleagueUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 동료 관계 확인 (권한 검증)
        Long idA = Math.min(myUserId, colleagueUserId);
        Long idB = Math.max(myUserId, colleagueUserId);

        Colleague colleague = colleagueRepository.findByPair(idA, idB)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        return ColleagueResponse.from(colleague, myUserId);
    }

    /**
     * DELETE /colleagues/{colleagueId} - 동료 삭제
     * <p>
     * colleagueId는 상대방 userId
     */
    @Transactional
    public void delete(Long colleagueUserId, Long myUserId) {
        // 상대방 존재 확인
        userRepository.findById(colleagueUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Long idA = Math.min(myUserId, colleagueUserId);
        Long idB = Math.max(myUserId, colleagueUserId);

        Colleague colleague = colleagueRepository.findByPair(idA, idB)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        colleagueRepository.delete(colleague);
    }
}
