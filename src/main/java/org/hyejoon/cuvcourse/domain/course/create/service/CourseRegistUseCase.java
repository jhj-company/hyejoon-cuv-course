package org.hyejoon.cuvcourse.domain.course.create.service;

import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;

public interface CourseRegistUseCase {

    CourseResponse registerCourse(long studentId, long lectureId);
}
