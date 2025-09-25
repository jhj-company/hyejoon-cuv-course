package org.hyejoon.cuvcourse.domain.lecture.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.entity.QCourse;
import org.hyejoon.cuvcourse.domain.lecture.entity.QLecture;
import org.hyejoon.cuvcourse.domain.lecture.lectureView.dto.LectureViewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureRepositoryCustomImpl implements LectureRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<LectureViewResponse> findLecturesWithTotal(Pageable pageable) {
        QLecture lecture = QLecture.lecture;
        QCourse course = QCourse.course;

        List<LectureViewResponse> results = queryFactory
            .select(Projections.constructor(
                LectureViewResponse.class,
                lecture.id,
                lecture.lectureTitle,
                lecture.professorName,
                lecture.credits,
//                course.id.student.countDistinct(),
                course.id.studentId.countDistinct(),
                lecture.capacity
            ))
            .from(lecture)
            .leftJoin(course).on(course.id.lectureId.eq(lecture.id))
            .groupBy(lecture.id,
                lecture.lectureTitle,
                lecture.professorName,
                lecture.credits,
                lecture.capacity)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(lecture.count())
            .from(lecture)
            .fetchOne();

        long totalCount = total != null ? total : 0L;

        return new PageImpl<>(results, pageable, totalCount);
    }
}
