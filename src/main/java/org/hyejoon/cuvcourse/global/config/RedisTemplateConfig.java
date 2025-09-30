package org.hyejoon.cuvcourse.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {

    /**
     * 강의 정원 검증을 위한 RedisTemplate을 Bean으로 등록
     *
     * @param redisConnectionFactory Spring이 관리하는 RedisConnectionFactory를 주입
     * @return String을 Key로, Long을 Value로 사용하는 Custom RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Long> courseCapacityRedisTemplate(
        RedisConnectionFactory redisConnectionFactory
    ) {

        // Redis 연결 설정
        RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Key와 Hash Key를 String으로 저장
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // Value와 Hash Value를 Long으로 저장 (INCR/DECR 정수 연산 지원)
        GenericToStringSerializer<Long> longSerializer = new GenericToStringSerializer<>(
            Long.class);

        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(longSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(longSerializer);

        // 명령을 모아 한꺼번에 실행하는 MULTI/EXEC 트랜잭션 사용 안함 (즉시 실행)
        // 캐시 접근은 빠르게 하고, 데이터 일관성은 애플리케이션 레벨에서 동기화 훅으로 관리
        redisTemplate.setEnableTransactionSupport(false);

        // 설정한 Connection & Serializers 적용하도록 초기화
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
