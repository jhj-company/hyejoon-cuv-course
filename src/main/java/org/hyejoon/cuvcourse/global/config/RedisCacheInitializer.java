package org.hyejoon.cuvcourse.global.config;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
@RequiredArgsConstructor
@Slf4j
public class RedisCacheInitializer {

    private final RedisTemplate<String, Long> redisTemplate;

    @EventListener(ApplicationStartedEvent.class)
    public void resetCacheOnStartup() {
        Set<String> keys = redisTemplate.keys("lecture:total:*");
        if (keys != null && !keys.isEmpty()) {
            log.info("RedisCacheInitializer 실행됨. 삭제할 키: {}", keys);
            redisTemplate.delete(keys);
        }
    }
}
