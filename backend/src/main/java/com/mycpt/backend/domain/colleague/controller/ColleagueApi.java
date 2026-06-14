package com.mycpt.backend.domain.colleague.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.colleague.dto.PeerCodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "동료", description = "동료 코드, 동료 등록/조회/삭제")
public interface ColleagueApi {

    @Operation(summary = "내 동료 코드 조회 (없거나 만료 시 자동 생성)")
    ResponseEntity<PeerCodeResponse> getPeerCode(
            @AuthenticationPrincipal UserPrincipal principal
    );

    @Operation(summary = "동료 코드 강제 갱신")
    ResponseEntity<PeerCodeResponse> refreshPeerCode(
            @AuthenticationPrincipal UserPrincipal principal
    );
}
