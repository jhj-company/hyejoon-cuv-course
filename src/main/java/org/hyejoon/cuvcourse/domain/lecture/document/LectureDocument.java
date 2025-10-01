package org.hyejoon.cuvcourse.domain.lecture.document;

import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(indexName = "lectures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long dbId;

    @Field(type = FieldType.Text)
    private String lectureTitle;

    @Field(type = FieldType.Keyword)
    private String professorName;

    @Field(type = FieldType.Integer)
    private Integer credits;

    @Field(type = FieldType.Integer)
    private Integer capacity;

    @Field(type = FieldType.Integer)
    private Integer total;

    public static LectureDocument from(Lecture lecture) {
        return new LectureDocument(
            lecture.getId().toString(),
            lecture.getId(),
            lecture.getLectureTitle(),
            lecture.getProfessorName(),
            lecture.getCredits(),
            lecture.getCapacity(),
            lecture.getTotal()
        );
    }
}
