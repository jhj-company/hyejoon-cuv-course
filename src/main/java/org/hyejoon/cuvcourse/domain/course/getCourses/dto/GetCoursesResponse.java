package org.hyejoon.cuvcourse.domain.course.getCourses.dto;

import java.util.List;

public record GetCoursesResponse(
    List<CourseResponse> courses,
    int enrolledCredits,
    int availableCredits
) {

}
