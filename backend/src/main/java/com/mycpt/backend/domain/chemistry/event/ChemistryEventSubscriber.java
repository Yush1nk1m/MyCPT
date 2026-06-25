package com.mycpt.backend.domain.chemistry.event;

import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.service.ChemistryCacheService;
import com.mycpt.backend.domain.chemistry.service.ChemistryCacheService.WaitingEntry;
import com.mycpt.backend.domain.notification.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis Pub/Sub 수신기.
 *
 * 메시지 수신 시:
 *   1. 채널 키에서 ChemistryCacheId 역직렬화
 *   2. ChemistryCacheService.releaseWaiters() 호출 → CountDownLatch 해제
 *   3. 대기자 목록 (userId, reportId)으로 SseService.pushChemistryEvent() 호출
 *
 * RedisConfig에서 RedisMessageListenerContainer에 등록됨.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChemistryEventSubscriber implements MessageListener {

    private final ChemistryCacheService chemistryCacheService;
    private final SseService sseService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        ChemistryCacheId cacheId = parseChannel(channel);
        if (cacheId == null) {
            log.warn("채널 파싱 실패. channel={}", channel);
            return;
        }

        // CountDownLatch 해제 + 대기자 목록 반환
        List<WaitingEntry> waiters = chemistryCacheService.releaseWaiters(cacheId);

        // 대기자 각각에게 SSE push
        boolean isReady = "READY".equals(body);
        for (WaitingEntry waiter : waiters) {
            if (isReady) {
                sseService.pushChemistryReady(waiter.userId(), waiter.reportId());
            } else {
                sseService.pushChemistryError(waiter.userId(), waiter.reportId());
            }
        }

        log.debug("Pub/Sub 수신 처리 완료. channel={}, body={}, waiters={}",
                channel, body, waiters.size());
    }

    private ChemistryCacheId parseChannel(String channel) {
        // 포맷: chemistry:{rd}:{ri}:{rs}:{rc}:{pd}:{pi}:{ps}:{pc}
        try {
            String[] parts = channel.split(":");
            if (parts.length != 9) return null;
            return new ChemistryCacheId(
                    Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]), Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]), Integer.parseInt(parts[6]),
                    Integer.parseInt(parts[7]), Integer.parseInt(parts[8])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
