package com.mycpt.backend.domain.assessment.controller;

import com.mycpt.backend.domain.assessment.dto.CreateTokenRequest;
import com.mycpt.backend.domain.assessment.dto.CreateTokenResponse;
import com.mycpt.backend.domain.assessment.dto.SubjectInfoResponse;
import com.mycpt.backend.domain.assessment.dto.SubmitResponse;
import com.mycpt.backend.domain.assessment.service.AssessmentService;
import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.result.dto.ScoreRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
public class AssessmentV1Controller implements AssessmentApi {

    private final AssessmentService assessmentService;

    // POST /api/v1/assessments - 회원 전용 (SecurityConfig에서 anyRequest().authenticated())
    @PostMapping
    @Override
    public ResponseEntity<CreateTokenResponse> createToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateTokenRequest request
    ) {
        AssessmentService.TokenInfo result =
                assessmentService.createToken(principal.getUser().getId(), request.label());
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateTokenResponse.from(result));
    }

    // GET /api/v1/assessments/{token} - 비회원 가능 (SecurityConfig permitAll)
    @GetMapping("/{token}")
    @Override
    public ResponseEntity<SubjectInfoResponse> getSubjectInfo(
            @PathVariable String token
    ) {
        AssessmentService.SubjectInfo info = assessmentService.getSubjectInfo(token);
        return ResponseEntity.ok(SubjectInfoResponse.from(info));
    }

    // POST /api/v1/assessments/{token}/submit - 비회원 가능 (SecurityConfig permitAll)
    @PostMapping("/{token}/submit")
    @Override
    public ResponseEntity<SubmitResponse> submit(
            @PathVariable String token,
            @RequestBody ScoreRequest request
    ) {
        assessmentService.submit(token, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SubmitResponse("응시가 완료되었습니다."));
    }
}
