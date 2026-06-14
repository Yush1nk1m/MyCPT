package com.mycpt.backend.domain.colleague.dto;

import com.mycpt.backend.domain.colleague.entity.PeerCode;

import java.time.LocalDateTime;

/**
 * GET /peer-code, POST /peer-code/refresh 공통 응답 DTO
 */
public record PeerCodeResponse(
        String code,
        LocalDateTime expiresAt
) {
    public static PeerCodeResponse from(PeerCode peerCode) {
        return new PeerCodeResponse(peerCode.getCode(), peerCode.getExpiresAt());
    }
}
