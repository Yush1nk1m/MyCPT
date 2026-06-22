package com.mycpt.backend.common.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Anthropic Java SDK 기반 공용 LLM 클라이언트.
 *
 * 기존 result/LlmService에서 API 호출 책임만 추출.
 * 프롬프트 빌드는 각 도메인 서비스(CacheService, ChemistryCacheService)가 담당.
 *
 * 클라이언트 생명주기:
 *   AnthropicClient는 커넥션 풀 + 스레드 풀을 보유하므로 애플리케이션 전역 1개 유지.
 *   @PostConstruct 초기화, @PreDestroy close().
 */
@Component
public class AnthropicLlmClient {

    private final String apiKey;
    private final String model;
    private final long maxTokens;

    private AnthropicClient client;

    public AnthropicLlmClient(
            @Value("${llm.anthropic.api-key}") String apiKey,
            @Value("${llm.anthropic.model:claude-sonnet-4-6}") String model,
            @Value("${llm.anthropic.max-tokens:3000}") long maxTokens
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    @PostConstruct
    void init() {
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @PreDestroy
    void destroy() throws Exception {
        if (client instanceof AutoCloseable c) {
            c.close();
        }
    }

    /**
     * 프롬프트를 받아 LLM 응답 텍스트를 반환.
     * SDK가 내부적으로 재시도(기본 2회), 타임아웃 처리.
     *
     * @param prompt 완성된 프롬프트 문자열
     * @return Markdown 형식 응답 텍스트
     */
    public String complete(String prompt) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.of(model))
                .maxTokens(maxTokens)
                .addUserMessage(prompt)
                .build();

        Message message = client.messages().create(params);

        // content() 리스트에서 text 블록만 추출 후 이어붙임
        // 보통 1개 블록이지만 복수 블록 대비 stream으로 처리
        return message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .reduce("", (a, b) -> a + b);
    }
}
