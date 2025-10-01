package org.hyejoon.cuvcourse.domain.lecture.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.hyejoon.cuvcourse.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "lectures")
public class Lecture extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lectureTitle;

    @Column(nullable = false)
    private String professorName;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int total;

    public Lecture(String lectureTitle, String professorName, int credits, int capacity) {
        this.lectureTitle = lectureTitle;
        this.professorName = professorName;
        this.credits = credits;
        this.capacity = capacity;
        this.total = 0;
    }

    public void increaseTotal() {
        if (this.total >= this.capacity) {
            throw new IllegalStateException("강의 정원이 가득 찼습니다.");
        }
        this.total += 1;
    }

    public void decreaseTotal() {
        if (this.total <= 0) {
            throw new IllegalStateException("수강 신청 인원이 음수가 될 수 없습니다.");
        }
        this.total -= 1;
    }
}
