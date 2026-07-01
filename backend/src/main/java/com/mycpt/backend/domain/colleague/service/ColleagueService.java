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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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

        // 비회원(myUserId==null)은 자기초대 검증 대상이 아님
        if (myUserId != null && inviter.getId().equals(myUserId)) {
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

        for (ColleagueResponse cr : result)
            log.info("Colleague's nickname: " + cr.nickname());

        return new ColleagueListResponse(result);
    }

    /**
     * GET /colleagues/{partnerId} - 동료 프로필 조회
     * <p>
     * partnerId는 colleagues.id가 아닌 상대방의 userId
     * (API 명세: partnerId = 조회할 동료의 userId)
     */
    @Transactional(readOnly = true)
    public ColleagueResponse get(Long partnerId, Long myUserId) {
        // 상대방이 존재하는지 확인
        userRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 동료 관계 확인 (권한 검증)
        Long idA = Math.min(myUserId, partnerId);
        Long idB = Math.max(myUserId, partnerId);

        Colleague colleague = colleagueRepository.findByPair(idA, idB)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        return ColleagueResponse.from(colleague, myUserId);
    }

    /**
     * DELETE /colleagues/{partnerId} - 동료 삭제
     * <p>
     * partnerId는 상대방 userId
     */
    @Transactional
    public void delete(Long partnerId, Long myUserId) {
        // 상대방 존재 확인
        userRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Long idA = Math.min(myUserId, partnerId);
        Long idB = Math.max(myUserId, partnerId);

        Colleague colleague = colleagueRepository.findByPair(idA, idB)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        colleagueRepository.delete(colleague);
    }
}
