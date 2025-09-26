package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;

public interface CourseRegistUseCase {

    CourseResponse registerCourse(long studentId, long lectureId);
}
