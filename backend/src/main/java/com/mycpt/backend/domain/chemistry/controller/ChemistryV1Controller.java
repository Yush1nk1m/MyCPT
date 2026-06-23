package com.mycpt.backend.domain.chemistry.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportDetail;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportListResponse;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportRequest;
import com.mycpt.backend.domain.chemistry.service.ChemistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chemistry-reports")
@RequiredArgsConstructor
public class ChemistryV1Controller implements ChemistryApi {

    private final ChemistryService chemistryService;

    @PostMapping
    @Override
    public ResponseEntity<Void> issue(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ChemistryReportRequest request
    ) {
        chemistryService.issue(
                principal.getUser().getId(),
                request.partnerId()
        );
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    @Override
    public ResponseEntity<ChemistryReportListResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                chemistryService.list(
                        principal.getUser().getId(),
                        partnerId,
                        cursor,
                        size
                )
        );
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ChemistryReportDetail> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                chemistryService.get(id, principal.getUser().getId())
        );
    }
}
