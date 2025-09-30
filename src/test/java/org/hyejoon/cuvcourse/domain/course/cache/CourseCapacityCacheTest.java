package org.hyejoon.cuvcourse.domain.course.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache.CourseCapacitySnapshot;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache.TryReserveResult;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CourseCapacityCacheTest {

    private static final long LECTURE_ID = 3L;
    private static final String CAPACITY_KEY = "course:lecture:%d:capacity".formatted(LECTURE_ID);
    private static final String HEADCOUNT_KEY = "course:lecture:%d:headcount".formatted(LECTURE_ID);

    @Mock
    private RedisTemplate<String, Long> courseCapacityRedisTemplate;

    @Mock
    private ValueOperations<String, Long> valueOperations;

    @Mock
    private CourseJpaRepository courseJpaRepository;

    @Mock
    private LectureJpaRepository lectureJpaRepository;

    @InjectMocks
    private CourseCapacityCache courseCapacityCache;

    private void stubOpsForValue() {
        when(courseCapacityRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 캐시에_정원과_신청인원이_있으면_DB를_조회하지_않는다() {
        stubOpsForValue();
        when(valueOperations.get(CAPACITY_KEY)).thenReturn(20L);
        when(valueOperations.get(HEADCOUNT_KEY)).thenReturn(5L);

        CourseCapacitySnapshot snapshot = courseCapacityCache.getOrInit(LECTURE_ID);

        assertThat(snapshot.capacity()).isEqualTo(20L);
        assertThat(snapshot.headcount()).isEqualTo(5L);
        verify(valueOperations, never()).setIfAbsent(anyString(), anyLong());
        verifyNoInteractions(lectureJpaRepository, courseJpaRepository);
    }

    @Test
    void 캐시_미스면_DB값으로_Redis를_초기화한다() {
        stubOpsForValue();
        when(valueOperations.get(CAPACITY_KEY)).thenReturn(null, 30L);
        when(valueOperations.get(HEADCOUNT_KEY)).thenReturn(null, 7L);

        Lecture lecture = new Lecture("테스트 강의", "테스트 교수", 3, 30);
        ReflectionTestUtils.setField(lecture, "id", LECTURE_ID);
        ReflectionTestUtils.setField(lecture, "total", 0);

        when(lectureJpaRepository.findById(LECTURE_ID)).thenReturn(Optional.of(lecture));
        when(courseJpaRepository.countByIdLecture(lecture)).thenReturn(7L);

        CourseCapacitySnapshot snapshot = courseCapacityCache.getOrInit(LECTURE_ID);

        assertThat(snapshot.capacity()).isEqualTo(30L);
        assertThat(snapshot.headcount()).isEqualTo(7L);
        verify(valueOperations).setIfAbsent(CAPACITY_KEY, 30L);
        verify(valueOperations).setIfAbsent(HEADCOUNT_KEY, 7L);
    }

    @Test
    void 강의가_없으면_예외를_던진다() {
        stubOpsForValue();
        when(valueOperations.get(CAPACITY_KEY)).thenReturn(null);
        when(valueOperations.get(HEADCOUNT_KEY)).thenReturn(null);
        when(lectureJpaRepository.findById(LECTURE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseCapacityCache.getOrInit(LECTURE_ID))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(CourseRegistExceptionEnum.LECTURE_NOT_FOUND.getMessage());
    }

    @Test
    void 예약_Lua가_성공코드를_주면_RESERVED를_반환한다() {
        when(courseCapacityRedisTemplate.execute(any(), eq(List.of(CAPACITY_KEY, HEADCOUNT_KEY))))
            .thenReturn(1L);

        TryReserveResult result = courseCapacityCache.tryReserve(LECTURE_ID);

        assertThat(result).isEqualTo(TryReserveResult.RESERVED);
    }

    @Test
    void 예약_Lua가_정원초과를_알리면_CAPACITY_FULL을_반환한다() {
        when(courseCapacityRedisTemplate.execute(any(), eq(List.of(CAPACITY_KEY, HEADCOUNT_KEY))))
            .thenReturn(0L);

        TryReserveResult result = courseCapacityCache.tryReserve(LECTURE_ID);

        assertThat(result).isEqualTo(TryReserveResult.CAPACITY_FULL);
    }

    @Test
    void 예약_Lua가_재시도를_요청하면_RETRY_LATER를_반환한다() {
        when(courseCapacityRedisTemplate.execute(any(), eq(List.of(CAPACITY_KEY, HEADCOUNT_KEY))))
            .thenReturn(-1L);

        TryReserveResult result = courseCapacityCache.tryReserve(LECTURE_ID);

        assertThat(result).isEqualTo(TryReserveResult.RETRY_LATER);
    }

    @Test
    void 예약_Lua가_이상한_코드를_주면_예외를_던진다() {
        when(courseCapacityRedisTemplate.execute(any(), eq(List.of(CAPACITY_KEY, HEADCOUNT_KEY))))
            .thenReturn(42L);

        assertThatThrownBy(() -> courseCapacityCache.tryReserve(LECTURE_ID))
            .isInstanceOf(CourseCapacityCacheException.class)
            .hasMessageContaining("Redis 수강 신청 예약 처리 중 예외가 발생했습니다. lecture=" + LECTURE_ID);
    }

    @Test
    void 취소_Lua가_키_미존재를_주면_예외를_던진다() {
        when(courseCapacityRedisTemplate.execute(any(), eq(List.of(HEADCOUNT_KEY))))
            .thenReturn(-1L);

        assertThatThrownBy(() -> courseCapacityCache.release(LECTURE_ID))
            .isInstanceOf(CourseCapacityCacheException.class)
            .hasMessageContaining("Redis 수강 신청 취소 처리 중 예외가 발생했습니다. lecture=" + LECTURE_ID);
    }

    @Test
    void 취소_Lua가_정상코드를_주면_성공한다() {
        when(courseCapacityRedisTemplate.execute(any(), eq(List.of(HEADCOUNT_KEY))))
            .thenReturn(1L);

        courseCapacityCache.release(LECTURE_ID);
    }
}
