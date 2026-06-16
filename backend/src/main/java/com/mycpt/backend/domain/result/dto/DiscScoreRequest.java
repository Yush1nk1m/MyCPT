package com.mycpt.backend.domain.result.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POST /results/score 요청 바디 DTO
 *
 * 요청 JSON 구조:
 * {
 *     "scores": { "d": 32, "i": 10, "s": -4, "c": 14 }
 * }
 *
 * record 사용 이유:
 *  - 불변 객체로 DTO의 의도를 명확히 표현 가능
 *  - equals/hashCode/toString 자동 생성
 *  - getter 자동 생성(d(), i(), s(), c())
 */
public record DiscScoreRequest(
        Scores scores
) {
    /**
     * DISC 원점수를 담는 중첩 레코드
     *
     * @JsonProperty: record 필드명이 단일 소문자이므로 Jackson이 JSON 키와 매핑할 수 있도록 명시적으로 지정
     */
    public record Scores(
            @JsonProperty("d") int d,
            @JsonProperty("i") int i,
            @JsonProperty("s") int s,
            @JsonProperty("c") int c
    ) {}
}
