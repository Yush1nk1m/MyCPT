package com.mycpt.backend.domain.result.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.mycpt.backend.domain.result.entity.DiscCacheId;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Anthropic Java SDK를 사용한 Claude API 호출 서비스.
 *
 * 이전 구현(java.net.http.HttpClient 직접 사용)과의 차이:
 *   - 인증 헤더, anthropic-version 헤더 등 SDK가 자동 처리
 *   - 요청/응답 직렬화 및 파싱을 SDK 타입으로 처리 → ObjectMapper 불필요
 *   - 재시도(기본 2회), 타임아웃, 커넥션 풀을 SDK가 관리
 *   - API 오류를 AnthropicServiceException 계층으로 받아 명시적 분기 가능
 *
 * 클라이언트 생명주기:
 *   SDK 문서 권고에 따라 AnthropicClient는 애플리케이션 전역에서 1개만 유지.
 *   (클라이언트 인스턴스마다 커넥션 풀과 스레드 풀을 보유하므로 공유가 효율적)
 *   → @PostConstruct에서 초기화, @PreDestroy에서 close().
 *
 * 설정값 (application.properties):
 *   llm.anthropic.api-key=${ANTHROPIC_API_KEY}
 *   llm.anthropic.model=claude-sonnet-4-6     (기본값)
 *   llm.anthropic.max-tokens=2000              (기본값)
 */
@Service
public class LlmService {

    private final String apiKey;
    private final String model;
    private final long maxTokens;

    // SDK 클라이언트 - @PostConstruct에서 초기화
    private AnthropicClient client;

    public LlmService(
            @Value("${llm.anthropic.api-key}") String apiKey,
            @Value("${llm.anthropic.model:claude-sonnet-4-6}") String model,
            @Value("${llm.anthropic.max-tokens:2000}") int maxTokens
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    @PostConstruct
    void init() {
        // AnthropicOkHttpClient.builder().apiKey()로 명시적 키 주입
        // fromEnv() 메서드를 사용하면 ANTHROPIC_API_KEY 환경변수를 자동으로 읽지만,
        // Spring @Value로 이미 외부화했으므로 명시적 주입 선택
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @PreDestroy
    void destroy() throws Exception {
        // AnthropicOkHttpClient는 AutoCloseable 구현체
        // 커넥션 풀, 스레드 풀 자원 해제
        if (client instanceof AutoCloseable c) {
            c.close();
        }
    }

    /**
     * DISC 버킷 값 기반 개인 분석 보고서 생성
     *
     * @param id 버킷 값 조합 (d, i, s, c 각 1~9)
     * @return Markdown 형식 보고서 (이름 미포함 - 렌더링 시 삽입)
     * @throws com.anthropic.errors.AnthropicServiceException   API 오류 시 (4xx/5xx)
     * @throws com.anthropic.errors.AnthropicIoException        네트워크 오류 시
     */
    public String generateReport(DiscCacheId id) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.of(model))
                .maxTokens(maxTokens)
                .addUserMessage(buildPrompt(id))
                .build();

        // client.messages().create()는 동기 호출
        // SDK가 내부적으로 재시도(기본 2회), 타임아웃 처리
        Message message = client.messages().create(params);

        // content() 리스트에서 text 블록만 추출 후 이어붙임. 보통 1개 블록이지만 복수 블록 대비 stream으로 처리
        return message.content().stream()
                .flatMap(block -> block.text().stream())    // ContentBlock -> TextBlock (Optional)
                .map(textBlock -> textBlock.text())
                .reduce("", (a, b) -> a + b);   // 빈 문자열 가드 포함
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * DISC 버킷 값을 기반으로 보고서 생성 프롬프트 구성
     *
     * 보고서 구조 (6개 섹션):
     *  ## 결과 개요
     *  ## 강점
     *  ## 약점 및 주의할 점
     *  ## 동료와의 협업 스타일
     *  ## 스트레스 상황에서의 반응
     *  ## 성장을 위한 제안
     *
     * 사용자 이름 미포함 원칙:
     *  "이 유형의 사람은"으로 서술 -> 동일 버킷 조합을 가진 모든 사용자에게 재사용 가능
     *  클라이언트가 렌더링 시 "{nickname}님의 강점" 식으로 이름을 삽입
     */
    private String buildPrompt(DiscCacheId id) {
        return """
                당신은 DISC 성격 유형 전문 분석가입니다.
                다음 DISC 버킷값을 기반으로 한국어 분석 보고서를 작성하세요.

                DISC 버킷값 (1=최하, 9=최상):
                - D (주도형 / Dominance):        %d
                - I (사교형 / Influence):         %d
                - S (안정형 / Steadiness):        %d
                - C (신중형 / Conscientiousness): %d

                다음 6개 섹션을 Markdown 형식으로 작성하세요.
                각 섹션은 3~5문장으로 구성하고, 특정 이름 없이 "이 유형의 사람은"으로 서술하세요.

                ## 결과 개요
                ## 강점
                ## 약점 및 주의할 점
                ## 동료와의 협업 스타일
                ## 스트레스 상황에서의 반응
                ## 성장을 위한 제안
                """.formatted(id.getD(), id.getI(), id.getS(), id.getC());
    }
}
