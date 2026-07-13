package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.result.dto.DiscScoreRequest;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    private static final int MIN_SCORE = -24;
    private static final int MAX_SCORE = 48;
    private static final int EXPECTED_SUM = 24;

    private static final int LOW_MAX = -5;
    private static final int MID_MAX = 11;

    public record Buckets(int d, int i, int s, int c) {}

    public Buckets normalize(DiscScoreRequest request) {
        throw new UnsupportedOperationException("stub");
    }

    private void validate(DiscScoreRequest.Scores scores) {
        throw new UnsupportedOperationException("stub");
    }

    int toBucket(int score) {
        throw new UnsupportedOperationException("stub");
    }
}
