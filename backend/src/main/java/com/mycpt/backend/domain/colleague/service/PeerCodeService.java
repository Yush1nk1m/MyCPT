package com.mycpt.backend.domain.colleague.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.colleague.dto.PeerCodeResponse;
import com.mycpt.backend.domain.colleague.entity.PeerCode;
import com.mycpt.backend.domain.colleague.repository.PeerCodeRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeerCodeService {

    private final PeerCodeRepository peerCodeRepository;
    private final UserRepository userRepository;

    @Value("${app.peer-code.ttl-days:7}")
    private long ttlDays;

    /**
     * GET /peer-code - 내 동료 코드 조회
     * <p>
     * 행 없음 -> 신규 생성 후 반환
     * 행 있음 + 유효 -> 기존 코드 반환
     * 행 있음 + 만료 -> 갱신 후 반환 (온디맨드 리프레시)
     */
    @Transactional
    public PeerCodeResponse getOrCreate(Long userId) {
        return peerCodeRepository.findByUserId(userId)
                .map(pc -> {
                    // 만료됐으면 갱신
                    if (pc.isExpired()) {
                        pc.refresh(ttlDays);
                        peerCodeRepository.save(pc);
                    }
                    return PeerCodeResponse.from(pc);
                })
                .orElseGet(() -> {
                    // 행 없으면 신규 생성
                    User user = userRepository.getReferenceById(userId);
                    PeerCode newCode = PeerCode.create(user, ttlDays);
                    peerCodeRepository.save(newCode);
                    return PeerCodeResponse.from(newCode);
                });
    }

    /**
     * POST /peer-code/refresh - 코드 강제 갱신
     * <p>
     * 유효 여부 무관하게 항상 새 코드 발급
     * 행 없으면 신규 생성 (방어적 처리 - 정상 사용 시 발생하지 않음)
     */
    @Transactional
    public PeerCodeResponse refresh(Long userId) {
        PeerCode pc = peerCodeRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return PeerCode.create(user, ttlDays);
                });

        pc.refresh(ttlDays);
        peerCodeRepository.save(pc);
        return PeerCodeResponse.from(pc);
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 5; ++attempt) {
            String code = PeerCode.generateCode();
            if (!peerCodeRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }
}
