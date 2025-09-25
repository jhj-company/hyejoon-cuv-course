package org.hyejoon.cuvcourse.domain.lecture.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lectureTitle;

    @Column(nullable = false)
    private String professorName;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int capacity;

    public Lecture (String lectureTitle, String professorName, int credits, int capacity) {
        this.lectureTitle = lectureTitle;
        this.professorName = professorName;
        this.credits = credits;
        this.capacity = capacity;
    }
}
