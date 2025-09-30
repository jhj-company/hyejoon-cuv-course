package org.hyejoon.cuvcourse.domain.lecture.cache;

import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureCacheService {

    public static final String LECTURE_CACHE_VALUE = "lectures";

    private final LectureJpaRepository lectureJpaRepository;

    @Cacheable(value = LECTURE_CACHE_VALUE, key = "#lectureId")
    public Lecture getLectureById(long lectureId) {
        return lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new BusinessException(LectureExceptionEnum.LECTURE_NOT_FOUND));
    }

    @Transactional
    @CachePut(value = LECTURE_CACHE_VALUE, key = "#lecture.id")
    public Lecture increaseLectureTotal(Lecture lecture) {
        lecture.increaseTotal();
        return lecture;
    }

    @Transactional
    @CachePut(value = LECTURE_CACHE_VALUE, key = "#lecture.id")
    public Lecture decreaseLectureTotal(Lecture lecture) {
        lecture.deceaseTotal();
        return lecture;
    }
}
