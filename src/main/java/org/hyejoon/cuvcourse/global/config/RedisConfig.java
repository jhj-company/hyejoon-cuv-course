package org.hyejoon.cuvcourse.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    public static final String LOCK_CONNECTION_FACTORY_BEAN_NAME = "lockRedisConnectionFactory";
    public static final String CACHE_CONNECTION_FACTORY_BEAN_NAME = "cacheRedisConnectionFactory";
    public static final String LOCK_STRING_TEMPLATE_BEAN_NAME = "lockStringRedisTemplate";

    private final RedisProperties redisProperties;

    @Bean
    @Qualifier(LOCK_CONNECTION_FACTORY_BEAN_NAME)
    public RedisConnectionFactory lockRedisConnectionFactory() {
        RedisProperties.Lock lock = redisProperties.getLock();
        return createLettuceConnectionFactory(lock.getHost(), lock.getPort(), "lock");
    }

    @Bean
    @Primary
    @Qualifier(CACHE_CONNECTION_FACTORY_BEAN_NAME)
    public RedisConnectionFactory cacheRedisConnectionFactory() {
        RedisProperties.Cache cache = redisProperties.getCache();
        return createLettuceConnectionFactory(cache.getHost(), cache.getPort(), "cache");
    }

    @Bean
    @Qualifier(LOCK_STRING_TEMPLATE_BEAN_NAME)
    public StringRedisTemplate lockStringRedisTemplate(
        @Qualifier(LOCK_CONNECTION_FACTORY_BEAN_NAME) RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    private static LettuceConnectionFactory createLettuceConnectionFactory(String host, int port,
        String type) {
        Assert.hasText(host, "redis." + type + ".host must not be empty");
        Assert.isTrue(port > 0, "redis." + type + ".port must be positive");
        return new LettuceConnectionFactory(host, port);
    }
}
