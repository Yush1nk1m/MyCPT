package com.mycpt.backend.domain.chemistry.event;

import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 케미 보고서 완료/실패 이벤트를 Redis Pub/Sub으로 발행.
 *
 * 채널 키 패턴: chemistry:{rd}:{ri}:{rs}:{rc}:{pd}:{pi}:{ps}:{pc}
 * 메시지 포맷: "READY" 또는 "ERROR"
 *
 * 발행 시점: READY 커밋 완료 직후 (새 요청이 READY를 먼저 보도록 커밋 선행)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChemistryEventPublisher {

    private final StringRedisTemplate redisTemplate;

    public void publishReady(ChemistryCacheId cacheId, String report) {
        String channel = toChannel(cacheId);
        redisTemplate.convertAndSend(channel, "READY");
        log.debug("케미 Pub/Sub 발행. channel={}", channel);
    }

    public void publishError(ChemistryCacheId cacheId) {
        String channel = toChannel(cacheId);
        redisTemplate.convertAndSend(channel, "ERROR");
        log.debug("케미 Pub/Sub 실패 발행. channel={}", channel);
    }

    public static String toChannel(ChemistryCacheId id) {
        return String.format("chemistry:%d:%d:%d:%d:%d:%d:%d:%d",
                id.getRequesterD(), id.getRequesterI(), id.getRequesterS(), id.getRequesterC(),
                id.getPartnerD(), id.getPartnerI(), id.getPartnerS(), id.getPartnerC()
        );
    }
}
