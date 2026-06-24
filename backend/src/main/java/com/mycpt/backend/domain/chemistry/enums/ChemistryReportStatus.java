package com.mycpt.backend.domain.chemistry.enums;

public enum ChemistryReportStatus {
    NULL,       // LLM 호출 전. report = null
    GENERATING, // LLM 호출 중. report = null
    READY,      // 발행 완료. report = TEXT
    ERROR,      // LLM 실패. report = null. 코인 환불됨
}
