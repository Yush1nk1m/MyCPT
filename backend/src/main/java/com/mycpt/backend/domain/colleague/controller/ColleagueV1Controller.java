package com.mycpt.backend.domain.colleague.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.colleague.dto.*;
import com.mycpt.backend.domain.colleague.service.ColleagueService;
import com.mycpt.backend.domain.colleague.service.PeerCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ColleagueV1Controller implements ColleagueApi {

    private final PeerCodeService peerCodeService;
    private final ColleagueService colleagueService;

    @GetMapping("/peer-code")
    @Override
    public ResponseEntity<PeerCodeResponse> getPeerCode(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                peerCodeService.getOrCreate(principal.getUser().getId())
        );
    }

    @PostMapping("/peer-code/refresh")
    @Override
    public ResponseEntity<PeerCodeResponse> refreshPeerCode(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                peerCodeService.refresh(principal.getUser().getId())
        );
    }

    @GetMapping("/colleagues/invite/{code}")
    @Override
    public ResponseEntity<InviteInfoResponse> getInviteInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code
    ) {
        return ResponseEntity.ok(
                colleagueService.getInviteInfo(code, principal.getUser().getId())
        );
    }

    @PostMapping("/colleagues")
    @Override
    public ResponseEntity<ColleagueResponse> register(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ColleagueRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                colleagueService.register(request.code(), principal.getUser().getId())
        );
    }

    @GetMapping("/colleagues")
    @Override
    public ResponseEntity<ColleagueListResponse> list(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(colleagueService.list(principal.getUser().getId()));
    }

    @GetMapping("/colleagues/{partnerId}")
    @Override
    public ResponseEntity<ColleagueResponse> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long partnerId
    ) {
        return ResponseEntity.ok(
                colleagueService.get(partnerId, principal.getUser().getId())
        );
    }

    @DeleteMapping("/colleagues/{partnerId}")
    @Override
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long partnerId
    ) {
        colleagueService.delete(partnerId, principal.getUser().getId());
        return ResponseEntity.ok().build();
    }
}
