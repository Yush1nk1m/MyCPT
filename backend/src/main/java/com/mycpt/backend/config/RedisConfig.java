package com.mycpt.backend.config;

import com.mycpt.backend.domain.chemistry.event.ChemistryEventPublisher;
import com.mycpt.backend.domain.chemistry.event.ChemistryEventSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Pub/Sub 설정.
 *
 * chemistry:* 패턴으로 모든 케미 이벤트 채널을 단일 리스너에서 수신.
 * 채널별 분기는 ChemistryEventSubscriber.onMessage() 내부에서 처리.
 */
@Configuration
public class RedisConfig {

    // ChemistryEventPublisher의 채널 포맷: chemistry:{8축 버킷 값}
    private static final String CHEMISTRY_CHANNEL_PATTERN = "chemistry:*";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ChemistryEventSubscriber chemistryEventSubscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                chemistryEventSubscriber,
                new PatternTopic(CHEMISTRY_CHANNEL_PATTERN)
        );
        return container;
    }
}
