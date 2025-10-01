package org.hyejoon.cuvcourse.domain.lecture.lecturesearch.dto;

import org.hyejoon.cuvcourse.domain.lecture.document.LectureDocument;

public record LectureSearchResponse(Long id, String lectureTitle, String professorName, int credits,
                                    int totalStudents, int capacity) {

    public static LectureSearchResponse of(LectureDocument document) {
        return new LectureSearchResponse(document.getDbId(), document.getLectureTitle(), document
            .getProfessorName(), document.getCredits(), document.getTotal(), document.getCapacity());
    }
}
