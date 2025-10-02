package org.hyejoon.cuvcourse.domain.course.getCourses.dto;


public record CourseResponse(
    Long lectureId,
    String lectureTitle,
    String professorName,
    int credits
) {

}
