package org.hyejoon.cuvcourse.domain.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// CourseId.java 파일 (Course 엔티티 내부에 @Embeddable로 정의되어 있을 수도 있음)

@Getter
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CourseId implements Serializable {

    // (Course 엔티티의 매핑에 따라 다를 수 있지만, 일반적으로 ID 필드를 가집니다.)
    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "student_id")
    private Long studentId;

    // 필요한 필드만 포함하는 생성자 (private)
    private CourseId(Long lectureId, Long studentId) {
        this.lectureId = lectureId;
        this.studentId = studentId;
    }

    // 💡 ID 값만 받는 정적 팩토리 메서드로 수정 (현재 코드의 목표)
    public static CourseId of(Long lectureId, Long studentId) {
        return new CourseId(lectureId, studentId);
    }
}