package org.hyejoon.cuvcourse.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    private final Lock lock = new Lock();
    private final Cache cache = new Cache();

    @Getter
    @Setter
    public static class Lock {

        private String host;
        private int port;
        private String registryKey;
        private long expiredMs;
    }

    @Getter
    @Setter
    public static class Cache {

        private String host;
        private int port;
        private long ttlMinutes = 5L;
    }
}
