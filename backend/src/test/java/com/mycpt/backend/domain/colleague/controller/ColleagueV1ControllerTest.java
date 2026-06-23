package com.mycpt.backend.domain.colleague.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycpt.backend.domain.colleague.dto.*;
import com.mycpt.backend.domain.colleague.service.ColleagueService;
import com.mycpt.backend.domain.colleague.service.PeerCodeService;
import com.mycpt.backend.support.MvcTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest: Controller + Security Filter Chain만 로드. Service는 @MockitoBean으로 대체.
// 비즈니스 예외 분기는 ColleagueServiceTest(UT)에서 검증하므로 이 테스트는 인증 분기만 다룬다.
@WebMvcTest(ColleagueV1Controller.class)
@DisplayName("ColleagueV1Controller 슬라이스 테스트")
class ColleagueV1ControllerTest extends MvcTestSupport {

    @MockitoBean
    private PeerCodeService peerCodeService;
    @MockitoBean
    private ColleagueService colleagueService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── GET /peer-code ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/peer-code")
    class GetPeerCode {

        @Test
        @DisplayName("[ST-ColleagueCtrl-코드조회-성공]")
        void 코드조회_성공() throws Exception {
            // given
            given(peerCodeService.getOrCreate(any()))
                    .willReturn(new PeerCodeResponse("AB3D9K2M", LocalDateTime.now()));

            // when
            mockMvc.perform(get("/api/v1/peer-code")
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("AB3D9K2M"));
        }

        @Test
        @DisplayName("[ST-ColleagueCtrl-코드조회-미인증]")
        void 코드조회_미인증() throws Exception {
            // when
            mockMvc.perform(get("/api/v1/peer-code"))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── POST /peer-code/refresh ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/peer-code/refresh")
    class RefreshPeerCode {

        @Test
        @DisplayName("[ST-ColleagueCtrl-코드갱신-성공]")
        void 코드갱신_성공() throws Exception {
            // given
            given(peerCodeService.refresh((any())))
                    .willReturn(new PeerCodeResponse("ZZ9K3D2M", LocalDateTime.now()));

            // when
            mockMvc.perform(post("/api/v1/peer-code/refresh")
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("ZZ9K3D2M"));
        }

        @Test
        @DisplayName("[ST-ColleagueCtrl-코드갱신-미인증]")
        void 코드갱신_미인증() throws Exception {
            // when
            mockMvc.perform(post("/api/v1/peer-code/refresh"))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /colleagues/invite/{code} ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/colleagues/invite/{code}")
    class GetInviteInfo {

        @Test
        @DisplayName("[ST-ColleagueCtrl-초대정보조회-성공]")
        void 초대정보조회_성공() throws Exception {
            // given
            given(colleagueService.getInviteInfo(any(), any()))
                    .willReturn(new InviteInfoResponse(1L, "유신", null));

            // when
            mockMvc.perform(get("/api/v1/colleagues/invite/{code}", "AB3D9K2M")
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nickname").value("유신"));
        }

        @Test
        @DisplayName("[ST-ColleagueCtrl-초대정보조회-미인증]")
        void 초대정보조회_미인증() throws Exception {
            // when
            mockMvc.perform(get("/api/v1/colleagues/invite/{code}", "AB3D9K2M"))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── POST /colleagues ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/colleagues")
    class Register {

        @Test
        @DisplayName("[ST-ColleagueCtrl-동료등록-성공]")
        void 동료등록_성공() throws Exception {
            // given
            given(colleagueService.register(any(), any()))
                    .willReturn(new ColleagueResponse(15L, "유신", null, LocalDateTime.now()));

            ColleagueRegisterRequest request = new ColleagueRegisterRequest("AB3D9K2M");

            // when
            mockMvc.perform(post("/api/v1/colleagues")
                            .with(authenticated(testUser()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    // then
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.partnerId").value(15));
        }

        @Test
        @DisplayName("[ST-ColleagueCtrl-동료등록-미인증]")
        void 동료등록_미인증() throws Exception {
            // given
            ColleagueRegisterRequest request = new ColleagueRegisterRequest("AB3D9K2M");

            // when
            mockMvc.perform(post("/api/v1/colleagues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .contentType(objectMapper.writeValueAsString(request)))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /colleagues ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/colleagues")
    class List_ {

        @Test
        @DisplayName("[ST-ColleagueCtrl-목록조회-성공]")
        void 목록조회_성공() throws Exception {
            // given
            ColleagueResponse item = new ColleagueResponse(15L, "유신", null, LocalDateTime.now());
            given(colleagueService.list(any()))
                    .willReturn(new ColleagueListResponse(List.of(item)));

            // when
            mockMvc.perform(get("/api/v1/colleagues")
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.colleagues[0].partnerId").value(15));
        }

        @Test
        @DisplayName("[ST-ColleagueCtrl-목록조회-미인증]")
        void 목록조회_미인증() throws Exception {
            // when
            mockMvc.perform(get("/api/v1/colleagues"))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /colleagues/{partnerId} ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/colleagues/{partnerId}")
    class Get {

        @Test
        @DisplayName("[ST-ColleagueCtrl-프로필조회-성공]")
        void 프로필조회_성공() throws Exception {
            // given
            given(colleagueService.get(any(), any()))
                    .willReturn(new ColleagueResponse(15L, "유신", null, LocalDateTime.now()));

            // then
            mockMvc.perform(get("/api/v1/colleagues/{partnerId}", 15L)
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.partnerId").value(15));
        }

        @Test
        @DisplayName("[ST-ColleagueCtrl-프로필조회-미인증]")
        void 프로필조회_미인증() throws Exception {
            // when
            mockMvc.perform(get("/api/v1/colleagues/{partnerId}", 15L))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── DELETE /colleagues/{partnerId} ─────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/colleagues/{partnerId}")
    class Delete {

        @Test
        @DisplayName("[ST-ColleagueCtrl-삭제-성공]")
        void 삭제_성공() throws Exception {
            // when
            mockMvc.perform(delete("/api/v1/colleagues/{partnerId}", 15L)
                            .with(authenticated(testUser())))
                    // then
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[ST-ColleagueCtrl-삭제-미인증]")
        void 삭제_미인증() throws Exception {
            // when
            mockMvc.perform(delete("/api/v1/colleagues/{partnerId}", 15L))
                    // then
                    .andExpect(status().isUnauthorized());
        }
    }
}