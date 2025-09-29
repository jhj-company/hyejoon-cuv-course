package org.hyejoon.cuvcourse.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.util.Assert;

@Configuration
@RequiredArgsConstructor
public class RedisLockConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisLockRegistry redisLockRegistry(
        @Qualifier(RedisConfig.LOCK_CONNECTION_FACTORY_BEAN_NAME) RedisConnectionFactory redisConnectionFactory) {
        RedisProperties.Lock lock = redisProperties.getLock();
        Assert.hasText(lock.getRegistryKey(), "redis.lock.registry-key must not be empty");
        Assert.isTrue(lock.getExpiredMs() > 0, "redis.lock.expired-ms must be positive");

        return new RedisLockRegistry(redisConnectionFactory, lock.getRegistryKey(), lock
            .getExpiredMs());
    }
}
