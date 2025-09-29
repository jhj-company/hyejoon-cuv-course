package org.hyejoon.cuvcourse.global.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

    private static final String REDISSON_HOST_PREFIX = "redis://";

    private final RedisProperties redisProperties;

    @Bean
    public RedissonClient redissonClient() {
        RedisProperties.Lock lock = redisProperties.getLock();
        Assert.hasText(lock.getHost(), "redis.lock.host must not be empty");
        Assert.isTrue(lock.getPort() > 0, "redis.lock.port must be positive");

        Config redissonConfig = new Config();
        String address = REDISSON_HOST_PREFIX + lock.getHost() + ":" + lock.getPort();
        redissonConfig.useSingleServer().setAddress(address);

        return Redisson.create(redissonConfig);
    }
}
