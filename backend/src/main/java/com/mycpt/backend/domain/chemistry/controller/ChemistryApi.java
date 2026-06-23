package com.mycpt.backend.domain.chemistry.controller;

import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportDetail;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportListResponse;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "케미", description = "케미 보고서 발행/조회")
@SecurityRequirement(name = "cookieAuth")
public interface ChemistryApi {

    @Operation(summary = "케미 보고서 발행 요청 (202 즉시 반환)")
    ResponseEntity<Void> issue(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ChemistryReportRequest request
    );

    @Operation(summary = "케미 보고서 이력 조회")
    ResponseEntity<ChemistryReportListResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "케미 보고서 상세 조회")
    ResponseEntity<ChemistryReportDetail> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    );
}
