package org.hyejoon.cuvcourse.domain.lecture.lectureView.dto;

public record LectureViewResponse (Long id, String lectureTitle, String professorName, int credits, int totalStudents, int capacity) {
}
