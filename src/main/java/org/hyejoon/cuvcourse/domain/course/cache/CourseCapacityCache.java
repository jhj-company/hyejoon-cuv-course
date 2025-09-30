package org.hyejoon.cuvcourse.domain.course.cache;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>- Redis 기반 강의 정원 캐시</p>
 * <p>- Lua 스크립트로 예약/해제 원자성 보장</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseCapacityCache {

    private static final String HEADCOUNT_KEY_PATTERN = "course:lecture:%d:headcount";
    private static final String CAPACITY_KEY_PATTERN = "course:lecture:%d:capacity";
    // 동기화 메타데이터(상태 등)을 저장할 키 패턴
    // status: ok, pending, error 중 1개
    // lastSyncAt: 마지막 동기화 시각
    // lastSyncBy: 마지막 동기화를 시도한 주체
    // errorCode: status가 error일 때의 에러 코드
    private static final String SYNC_KEY_PATTERN = "course:lecture:%d:sync";

    // Lua 스크립트 반환 코드
    private static final int SCRIPT_RESERVED = 1; // 예약 성공
    private static final int SCRIPT_CAPACITY_FULL = 0; // 정원 초과
    private static final int SCRIPT_RETRY_LATER = -1; // 캐시 초기화 필요(재시도 필요)
    private static final int SCRIPT_ERROR = -2; // 비정상 상태

    private static final int SCRIPT_RELEASE_OK = 1; // 해제 성공
    private static final int SCRIPT_RELEASE_NOOP = 0; // 해제 요청이지만 감소 필요 없음
    private static final int SCRIPT_RELEASE_MISSING = -1; // 해제 대상 키 없음

    private static final DefaultRedisScript<Long> TRY_RESERVE_SCRIPT; // 예약용 Lua 스크립트
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT; // 해제용 Lua 스크립트

    static {
        // 수강신청 예약 스크립트 객체
        DefaultRedisScript<Long> tryReserveScript = new DefaultRedisScript<>();
        tryReserveScript.setResultType(Long.class); // 결과 타입은 Long
        tryReserveScript.setScriptText("""
            local capacity = redis.call('GET', KEYS[1])
            if not capacity then
                return -1
            end
            
            capacity = tonumber(capacity)
            if not capacity then
                return -2
            end
            
            local headcount = redis.call('GET', KEYS[2])
            if not headcount then
                headcount = 0
            else
                headcount = tonumber(headcount)
                if not headcount then
                    return -2
                end
            end
            
            if headcount >= capacity then
                return 0
            end
            
            local reserved = redis.call('INCR', KEYS[2])
            if reserved > capacity then
                redis.call('DECR', KEYS[2])
                return 0
            end
            
            return 1
            """);
        TRY_RESERVE_SCRIPT = tryReserveScript;

        // 수강신청 취소 스크립트 객체
        DefaultRedisScript<Long> releaseScript = new DefaultRedisScript<>();
        releaseScript.setResultType(Long.class);
        releaseScript.setScriptText("""
            local headcount = redis.call('GET', KEYS[1])
            if not headcount then
                return -1
            end
            
            headcount = tonumber(headcount)
            if not headcount then
                return -2
            end
            
            if headcount <= 0 then
                return 0
            end
            
            redis.call('DECR', KEYS[1])
            return 1
            """);
        RELEASE_SCRIPT = releaseScript;
    }

    private final RedisTemplate<String, Long> courseCapacityRedisTemplate;
    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;

    private static String formatHeadcountKey(long lectureId) {
        return HEADCOUNT_KEY_PATTERN.formatted(lectureId);
    }

    private static String formatCapacityKey(long lectureId) {
        return CAPACITY_KEY_PATTERN.formatted(lectureId);
    }

    @SuppressWarnings("unused")
    private static String formatSyncKey(long lectureId) {
        return SYNC_KEY_PATTERN.formatted(lectureId);
    }

    /**
     * 캐시에서 정원/신청 인원 조회, 없으면 DB에서 조회 후 캐시 초기화 ({@code Cache-aside})
     *
     * @param lectureId 강의 ID
     * @return 정원/신청 인원 스냅샷
     */
    @Transactional(readOnly = true)
    public CourseCapacitySnapshot getOrInit(long lectureId) {
        String capacityKey = formatCapacityKey(lectureId);
        String headcountKey = formatHeadcountKey(lectureId);

        Long cachedCapacity = readValue(capacityKey, lectureId, "정원(capacity) 캐시 조회");
        Long cachedHeadcount = readValue(headcountKey, lectureId, "신청 인원(headcount) 캐시 조회");

        if (cachedCapacity != null && cachedHeadcount != null) {
            return new CourseCapacitySnapshot(cachedCapacity, cachedHeadcount);
        }

        Lecture lecture = lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new CourseCapacityCacheException(
                "캐시 초기화를 위한 강의 정보를 찾을 수 없습니다. lecture=" + lectureId
            ));

        long capacityFromDb = lecture.getCapacity();
        long headcountFromDb = courseJpaRepository.countByIdLecture(lecture);

        if (cachedCapacity == null) {
            writeIfAbsent(capacityKey, capacityFromDb, lectureId, "정원(capacity) 초기화");
            cachedCapacity = readValue(capacityKey, lectureId, "정원(capacity) 재조회");
        }

        if (cachedHeadcount == null) {
            writeIfAbsent(headcountKey, headcountFromDb, lectureId, "신청 인원(headcount) 초기화");
            cachedHeadcount = readValue(headcountKey, lectureId, "신청 인원(headcount) 재조회");
        }

        long resolvedCapacity = cachedCapacity != null ? cachedCapacity : capacityFromDb;
        long resolvedHeadcount = cachedHeadcount != null ? cachedHeadcount : headcountFromDb;

        return new CourseCapacitySnapshot(resolvedCapacity, resolvedHeadcount);
    }

    // 수강 신청 시도한 후 결과값 반환
    public TryReserveResult tryReserve(long lectureId) {
        String capacityKey = formatCapacityKey(lectureId);
        String headcountKey = formatHeadcountKey(lectureId);

        try {
            Long result = courseCapacityRedisTemplate.execute(
                TRY_RESERVE_SCRIPT,
                List.of(capacityKey, headcountKey)
            );

            if (result == null) {
                throw new CourseCapacityCacheException(
                    "수강 신청 예약 Lua 스크립트 실행 결과가 null 입니다. lecture=" + lectureId
                );
            }

            int code = result.intValue();
            return switch (code) {
                case SCRIPT_RESERVED -> TryReserveResult.RESERVED;
                case SCRIPT_CAPACITY_FULL -> TryReserveResult.CAPACITY_FULL;
                case SCRIPT_RETRY_LATER -> TryReserveResult.RETRY_LATER;
                case SCRIPT_ERROR -> throw new CourseCapacityCacheException(
                    "수강 신청 예약 Lua 스크립트 실행 중 파싱 오류가 발생했습니다. lecture=" + lectureId
                );
                default -> throw new CourseCapacityCacheException(
                    "수강 신청 예약 Lua 스크립트가 예상치 못한 코드(" + code + ")를 반환했습니다. lecture="
                        + lectureId
                );
            };
        } catch (RuntimeException ex) {
            log.error(
                "Redis 수강 신청 예약 처리 중 예외 발생. lectureId={}",
                lectureId,
                ex
            );
            throw new CourseCapacityCacheException(
                "Redis 수강 신청 예약 처리 중 예외가 발생했습니다. lecture=" + lectureId,
                ex
            );
        }
    }

    // 수강 신청 취소 시 예약 해제
    public void release(long lectureId) {
        String headcountKey = formatHeadcountKey(lectureId);

        try {
            Long result = courseCapacityRedisTemplate.execute(
                RELEASE_SCRIPT,
                List.of(headcountKey)
            );

            if (result == null) {
                throw new CourseCapacityCacheException(
                    "수강 신청 취소 Lua 스크립트 실행 결과가 null 입니다. lecture=" + lectureId
                );
            }

            int code = result.intValue();
            if (code == SCRIPT_RELEASE_OK || code == SCRIPT_RELEASE_NOOP) {
                return;
            }

            if (code == SCRIPT_RELEASE_MISSING) {
                throw new CourseCapacityCacheException(
                    "수강 신청 취소 시 Redis에 headcount 키가 존재하지 않습니다. lecture=" + lectureId
                );
            }

            if (code == SCRIPT_ERROR) {
                throw new CourseCapacityCacheException(
                    "수강 신청 취소 Lua 스크립트 실행 중 파싱 오류가 발생했습니다. lecture=" + lectureId
                );
            }

            throw new CourseCapacityCacheException(
                "수강 신청 취소 Lua 스크립트가 예상치 못한 코드(" + code + ")를 반환했습니다. lecture=" + lectureId
            );
        } catch (RuntimeException ex) {
            log.error(
                "Redis 수강 신청 취소 처리 중 예외 발생. lectureId={}",
                lectureId,
                ex
            );
            throw new CourseCapacityCacheException(
                "Redis 수강 신청 취소 처리 중 예외가 발생했습니다. lecture=" + lectureId,
                ex
            );
        }
    }

    // Redis에서 단일 값 조회
    private Long readValue(String key, long lectureId, String action) {
        try {
            return courseCapacityRedisTemplate.opsForValue().get(key);
        } catch (RuntimeException ex) {
            log.error(
                "Redis {} 중 예외 발생. lectureId={}, key={}",
                action, lectureId, key,
                ex
            );
            throw new CourseCapacityCacheException(
                "Redis " + action + " 중 예외가 발생했습니다. lecture=" + lectureId,
                ex
            );
        }
    }

    // Redis에 단일 값 쓰기 (없을 때만)
    private void writeIfAbsent(String key, long value, long lectureId, String action) {
        try {
            courseCapacityRedisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (RuntimeException ex) {
            log.error(
                "Redis {} 중 예외 발생. lectureId={}, key={}, value={}",
                action, lectureId, key, value,
                ex
            );
            throw new CourseCapacityCacheException(
                "Redis " + action + " 중 예외가 발생했습니다. lecture=" + lectureId,
                ex
            );
        }
    }

    public enum TryReserveResult {
        RESERVED,
        CAPACITY_FULL,
        RETRY_LATER,
        ERROR
    }

    public record CourseCapacitySnapshot(long capacity, long headcount) {

    }
}
