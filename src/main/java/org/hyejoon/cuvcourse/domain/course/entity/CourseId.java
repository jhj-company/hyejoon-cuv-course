package org.hyejoon.cuvcourse.domain.course.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.student.entity.Student;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
public class CourseId implements Serializable {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Lecture lecture;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Student student;


    public static CourseId of(Lecture lecture, Student student) {
        return new CourseId(lecture, student);

    }
}
