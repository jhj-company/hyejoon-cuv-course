package org.hyejoon.cuvcourse.testinfra;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Redis 연결이 필요한 테스트에서 재사용하는 Testcontainers 설정.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class RedisTestContainers {

    private static final int REDIS_PORT = 6379;

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
        DockerImageName.parse("redis:7.2.4")
    )
        .withExposedPorts(REDIS_PORT)
        .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void overrideRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT));
    }
}
