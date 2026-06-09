package com.mycpt.backend.domain.assessment.controller;

import com.mycpt.backend.domain.assessment.dto.CreateTokenRequest;
import com.mycpt.backend.domain.assessment.service.AssessmentService;
import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.result.dto.ScoreRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
public class AssessmentV1Controller implements AssessmentApi {

    private final AssessmentService assessmentService;

    // POST /api/v1/assessments - 회원 전용 (SecurityConfig에서 anyRequest().authenticated())
    @PostMapping
    @Override
    public ResponseEntity<Map<String, Object>> createToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateTokenRequest request
    ) {
        AssessmentService.TokenResult result =
                assessmentService.createToken(principal.getUser().getId(), request.label());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("token", result.token());
        body.put("expiresAt", result.expiresAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // GET /api/v1/assessments/{token} - 비회원 가능 (SecurityConfig permitAll)
    @GetMapping("/{token}")
    @Override
    public ResponseEntity<Map<String, Object>> getSubjectInfo(
            @PathVariable String token
    ) {
        AssessmentService.SubjectInfo info = assessmentService.getSubjectInfo(token);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("subjectNickname", info.subjectNickname());
        body.put("subjectProfileImageUrl", info.subjectProfileImageUrl());
        return ResponseEntity.ok(body);
    }

    // POST /api/v1/assessments/{token}/submit - 비회원 가능 (SecurityConfig permitAll)
    @PostMapping("/{token}/submit")
    @Override
    public ResponseEntity<Map<String, String>> submit(
            @PathVariable String token,
            @RequestBody ScoreRequest request
    ) {
        assessmentService.submit(token, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "응시가 완료되었습니다."));
    }
}
