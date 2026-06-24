package com.mycpt.backend.domain.chemistry.enums;

public enum ChemistryCacheStatus {
    NULL,        // 미생성. SELECT FOR UPDATE 후 이 값을 발견한 스레드가 발행자
    GENERATING,  // 발행자가 LLM 호출 중. 이 값을 발견한 스레드는 구독자 → Redis Pub/Sub 대기
    READY        // 보고서 생성 완료. 이 값을 발견한 스레드는 즉시 report 반환
}
