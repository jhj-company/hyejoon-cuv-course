package org.hyejoon.cuvcourse.domain.course.cousecancle.controller;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.cousecancle.service.CourseCancleService;
import org.hyejoon.cuvcourse.global.auth.AuthConstant;
import org.hyejoon.cuvcourse.global.dto.GlobalResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CourseCancleController {

    private final CourseCancleService couseCancleService;

    @DeleteMapping("/api/courses/{lectureId}")
    public GlobalResponse<?> deleteCourse(
        @PathVariable("lectureId") Long lectureId,
        @RequestHeader(AuthConstant.X_STUDENT_ID) Long studentId
    ) {
        couseCancleService.courseCancle(lectureId, studentId);

        return GlobalResponse.ok("수강 신청이 취소되었습니다.", null);
    }
}
