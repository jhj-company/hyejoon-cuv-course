package org.hyejoon.cuvcourse.domain.lecture.entity;

import org.hyejoon.cuvcourse.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Lecture extends BaseTimeEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private String lectureTitle;

    @Column(nullable = false)
    private String professorName;

    @Column(nullable = false)
    private int credits = 0;

    @Column(nullable = false)
    private int capacity = 0;

    @Column(nullable = false)
    private int total = 0;
}
