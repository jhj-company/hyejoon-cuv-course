package org.hyejoon.cuvcourse.domain.course.cousecancel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache.CourseCapacitySnapshot;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityTransactionalFacade;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CourseCancelTxServiceTest {

    private final CourseJpaRepository courseJpaRepository = Mockito.mock(CourseJpaRepository.class);
    private final CourseCapacityCache courseCapacityCache = mock(CourseCapacityCache.class);
    private final CourseCapacityTransactionalFacade transactionalFacade = new CourseCapacityTransactionalFacade();
    private final LectureJpaRepository lectureJpaRepository = Mockito.mock(
        LectureJpaRepository.class);

    private final CourseCancelTxService courseCancelTxService = new CourseCancelTxService(
        courseJpaRepository,
        courseCapacityCache,
        transactionalFacade,
        lectureJpaRepository
    );

    @Test
    void 수강_취소_시_강의_total_값이_감소한다() throws Exception {
        Lecture lecture = new Lecture("자료구조", "김교수", 3, 30);
        setField(lecture, "id", 1L);
        setField(lecture, "total", 1);

        Student student = createStudent(10L);
        CourseId courseId = CourseId.of(lecture, student);
        Course course = Course.from(courseId);

        when(courseJpaRepository.findByLectureAndStudent(1L, 10L)).thenReturn(Optional.of(course));
        when(lectureJpaRepository.findById(1L)).thenReturn(Optional.of(lecture));
        when(courseCapacityCache.getOrInit(1L)).thenReturn(new CourseCapacitySnapshot(30, 1));
        doNothing().when(courseJpaRepository).delete(course);
        doNothing().when(courseCapacityCache).release(1L);

        courseCancelTxService.cancelWithTransaction(1L, 10L);

        assertThat(lecture.getTotal()).isEqualTo(0);
    }

    private Student createStudent(Long id) throws Exception {
        Constructor<Student> constructor = Student.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Student student = constructor.newInstance();
        setField(student, "id", id);
        return student;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
