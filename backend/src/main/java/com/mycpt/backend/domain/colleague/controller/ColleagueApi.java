package com.mycpt.backend.domain.colleague.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.colleague.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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

    @Operation(summary = "초대 코드로 초대자 정보 조회")
    ResponseEntity<InviteInfoResponse> getInviteInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code
    );

    @Operation(summary = "동료 등록")
    ResponseEntity<ColleagueResponse> register(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ColleagueRegisterRequest request
    );

    @Operation(summary = "동료 목록 조회")
    ResponseEntity<ColleagueListResponse> list(
            @AuthenticationPrincipal UserPrincipal principal
    );

    @Operation(summary = "동료 프로필 조회")
    ResponseEntity<ColleagueResponse> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long partnerId
    );

    @Operation(summary = "동료 삭제")
    ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long partnerId
    );
}
