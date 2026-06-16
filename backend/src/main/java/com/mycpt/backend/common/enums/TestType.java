package com.mycpt.backend.common.enums;

/**
 * 검사 유형 열거형 — 도메인 간 공유 타입
 *
 * 사용처: ChemistryReport.testType — "이 보고서가 어떤 검사 기반인가"라는 도메인 의미
 *
 * DTO(ScoreRequest/ScoreResponse)에는 사용하지 않음.
 * 이유: 검사 유형마다 점수 구조(scores 객체)가 다르기 때문에
 * 하나의 DTO로 묶으면 DISC 점수를 MBTI 레이블로 요청하는 것을 막을 수 없음.
 * MBTI 추가 시 MbtiScoreRequest 전용 DTO + 전용 엔드포인트로 확장.
 *
 * tests.dtype(@DiscriminatorColumn)과 동일한 문자열("DISC")을 사용하나 역할이 다름.
 * dtype은 JPA 다형 조회용 내부 식별자, TestType은 도메인 계층의 검사 유형 개념.
 */
public enum TestType {
    DISC,
    // MBTI,    // 향후 확장 시 추가
    // BIG5,
}
