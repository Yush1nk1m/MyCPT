package com.mycpt.backend.domain.chemistry.dto;

import java.util.List;

public record ChemistryReportListResponse(
        List<ChemistryReportSummary> reports,
        Long nextCursor,
        boolean hasNext
) {}
