package com.mycpt.backend.domain.colleague.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.colleague.dto.PeerCodeResponse;
import com.mycpt.backend.domain.colleague.service.PeerCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ColleagueV1Controller implements ColleagueApi {

    private final PeerCodeService peerCodeService;

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
}
