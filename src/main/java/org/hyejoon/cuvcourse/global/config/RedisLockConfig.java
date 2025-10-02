package org.hyejoon.cuvcourse.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

@Configuration
public class RedisLockConfig {

    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory,
        @Value("${lock.redis.registry-key}") String registryKey,
        @Value("${lock.redis.expired-ms}") long lockExpiredMs) {
        return new RedisLockRegistry(redisConnectionFactory, registryKey, lockExpiredMs);
    }
}
