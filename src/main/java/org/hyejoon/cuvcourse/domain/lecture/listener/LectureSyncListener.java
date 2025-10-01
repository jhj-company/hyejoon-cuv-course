package org.hyejoon.cuvcourse.domain.lecture.listener;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.lecture.document.LectureDocument;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.event.LectureUpdatedEvent;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureESRepository;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LectureSyncListener {

    private final LectureJpaRepository lectureJpaRepository;
    private final LectureESRepository lectureESRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLectureUpdatedEvent(LectureUpdatedEvent event) {
        lectureJpaRepository.findById(event.id()).ifPresent(this::syncLectureToES);
    }

    private void syncLectureToES(Lecture lecture) {
        LectureDocument lectureDocument = LectureDocument.from(lecture);
        lectureESRepository.save(lectureDocument);
    }
}
