package org.hyejoon.cuvcourse.domain.lecture.dto;

public record LectureViewResponse (Long id, String lectureTitle, String professorName, int credits, int capacity, int total) {
}
