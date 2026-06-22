package com.mycpt.backend.domain.coin.controller;

import com.mycpt.backend.domain.coin.dto.CoinBalanceResponse;
import com.mycpt.backend.domain.coin.dto.CoinHistoryResponse;
import com.mycpt.backend.domain.coin.dto.CoinTransactionResponse;
import com.mycpt.backend.domain.coin.service.CoinService;
import com.mycpt.backend.support.MvcTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoinV1Controller.class)
@DisplayName("CoinV1Controller 슬라이스 테스트")
class CoinV1ControllerTest extends MvcTestSupport {

    @MockitoBean
    private CoinService coinService;

    // ── GET /coins ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/coins")
    class GetBalance {

        @Test
        @DisplayName("[ST-CoinCtrl-잔액조회-성공]")
        void 잔액조회_성공() throws Exception {
            // given
            given(coinService.getBalance(any()))
                    .willReturn(new CoinBalanceResponse(2, LocalDateTime.now().plusHours(10)));

            // when
            mockMvc.perform(get("/api/v1/coins")
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.coins").value(2));
        }

        @Test
        @DisplayName("[ST-CoinCtrl-잔액조회-미인증]")
        void 잔액조회_미인증() throws Exception {
            // when
            mockMvc.perform(get("/api/v1/coins"))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /coins/history ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/coins/history")
    class GetHistory {

        @Test
        @DisplayName("[ST-CoinCtrl-이력조회-성공]")
        void 이력조회_성공() throws Exception {
            // given
            CoinTransactionResponse item = new CoinTransactionResponse(
                    1L, -1, "CHEMISTRY_REPORT", 2, LocalDateTime.now()
            );
            given(coinService.getHistory(any(), any(), anyInt()))
                    .willReturn(new CoinHistoryResponse(List.of(item), null, false));

            // when
            mockMvc.perform(get("/api/v1/coins/history")
                            .with(authenticated(testUser())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.history[0].transactionId").value(1))
                    .andExpect(jsonPath("$.history[0].reason").value("CHEMISTRY_REPORT"));
        }

        @Test
        @DisplayName("[ST-CoinCtrl-이력조회-미인증]")
        void 이력조회_미인증() throws Exception {
            // when
            mockMvc.perform(get("/api/v1/coins/history"))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }
}