package org.hyejoon.cuvcourse.domain.course.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hyejoon.cuvcourse.global.entity.BaseTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "courses")
public class Course extends BaseTimeEntity {

    @EmbeddedId
    private CourseId id;

    public static Course from(CourseId courseId) {
        return new Course(courseId);
    }

}
