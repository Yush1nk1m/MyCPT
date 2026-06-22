package com.mycpt.backend.domain.chemistry.dto;

// POST /chemistry-reports 요청
public record ChemistryReportRequest(
        Long partnerId  // 케미 보고서 대상 동료의 userId
) {}
