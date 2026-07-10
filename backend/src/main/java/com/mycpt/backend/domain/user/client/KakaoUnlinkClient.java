package com.mycpt.backend.domain.user.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * 카카오 연결 해제(unlink) 클라이언트.
 * Admin Key 기반 — 사용자 액세스/리프레시 토큰을 별도로 저장하지 않고
 * 서버가 보유한 Admin Key + kakaoId만으로 연결 해제 처리.
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink
 */
@Slf4j
@Component
public class KakaoUnlinkClient {

    private final RestClient restClient;
    private final String adminKey;

    public KakaoUnlinkClient(@Value("${kakao.admin-key}") String adminKey) {
        this.adminKey = adminKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }

    /**
     * @param kakaoId 연결 해제할 사용자의 카카오 회원번호
     */
    public void unlink(String kakaoId) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", kakaoId);

        // TODO: 롤백 유도 대신 로그 기반 unlink 재시도 로직으로 변경 필요
        // 실패 시 예외를 던져 UserService.withdraw()의 @Transactional 롤백으로 연결
        // - 카카오 연결은 남아있지만 DB 데이터만 삭제되는 상태를 방지
        try {
            restClient.post()
                    .uri("/v1/user/unlink")
                    .header("Authorization", "KakaoAK " + adminKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("카카오 연결 해제 실패. kakaoId={}", kakaoId, e);
            throw e;
        }
    }
}
