package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityCache;
import org.hyejoon.cuvcourse.domain.course.cache.CourseCapacityTransactionalFacade;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CourseRegistTxServiceTest {

    private CourseJpaRepository courseJpaRepository;
    private CourseCapacityCache courseCapacityCache;
    private CourseCapacityTransactionalFacade courseCapacityTransactionalFacade;
    private LectureJpaRepository lectureJpaRepository;
    private CourseRegistTxService courseRegistTxService;

    @BeforeEach
    void setUp() {
        courseJpaRepository = Mockito.mock(CourseJpaRepository.class);
        courseCapacityCache = mock(CourseCapacityCache.class);
        courseCapacityTransactionalFacade = new CourseCapacityTransactionalFacade();
        lectureJpaRepository = Mockito.mock(LectureJpaRepository.class);

        courseRegistTxService = new CourseRegistTxService(
            courseJpaRepository,
            courseCapacityCache,
            courseCapacityTransactionalFacade,
            lectureJpaRepository
        );
    }

    @Test
    void DB_폴백_경로_수강_신청_시_강의_total_값이_증가한다() throws Exception {
        Lecture lecture = new Lecture("자료구조", "김교수", 3, 30);
        setField(lecture, "id", 1L);
        Student student = createStudent(10L);
        CourseId courseId = CourseId.of(lecture, student);

        when(lectureJpaRepository.findById(1L)).thenReturn(Optional.of(lecture));
        when(courseJpaRepository.countByIdLecture(lecture)).thenReturn(0L);
        when(courseJpaRepository.save(any(Course.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        courseRegistTxService.createCourseWithoutCache(lecture, courseId);

        assertThat(lecture.getTotal()).isEqualTo(1);
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
