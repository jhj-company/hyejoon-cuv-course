package org.hyejoon.cuvcourse.domain.course.entity;

import org.hyejoon.cuvcourse.global.entity.BaseTimeEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Course extends BaseTimeEntity {

    @EmbeddedId
    private CourseId id;

}
