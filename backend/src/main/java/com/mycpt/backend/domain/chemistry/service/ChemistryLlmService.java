package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.coin.service.CoinService;
import com.mycpt.backend.domain.notification.service.NotificationService;
import com.mycpt.backend.domain.statistics.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChemistryLlmService {

    private final ChemistryReportRepository chemistryReportRepository;
    private final StatisticsRepository statisticsRepository;
    private final CoinService coinService;
    private final NotificationService notificationService;

    @Value("${anthropic.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL   = "claude-sonnet-4-6";
}
