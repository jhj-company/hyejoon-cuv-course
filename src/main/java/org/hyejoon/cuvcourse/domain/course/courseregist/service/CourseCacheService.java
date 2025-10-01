package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseCacheService {

    private final RedisTemplate<String, Long> redisTemplate;
    private final CourseJpaRepository courseJpaRepository;

    private String getCacheKey(Long lectureId) {
        return "lecture:total:" + lectureId;
    }

    /**
     * 캐시에서 현재 수강 인원 조회. 캐시가 없으면 DB에서 조회 후 캐시에 초기값 세팅
     */
    public long getCurrentHeadcount(Lecture lecture) {
        String key = getCacheKey(lecture.getId());
        Long cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("getCurrentHeadcount from cache: {}", cached);
            return cached;
        }
        // 캐시 없으면 DB에서 조회
        long total = courseJpaRepository.countByIdLecture(lecture);
        redisTemplate.opsForValue().set(key, total);
        log.info("getCurrentHeadcount from DB: {}", total);
        return total;
    }

    /**
     * 캐시에서 수강 인원 1 증가 (atomic)
     */
    public long incrementHeadcount(Long lectureId) {
        String key = getCacheKey(lectureId);
        Long newTotal = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(30));
        log.info("incrementHeadcount: lectureId={} -> total={}", lectureId, newTotal);
        return newTotal;
    }
}
