package org.hyejoon.cuvcourse.domain.course.cousecancel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.cousecancel.dto.CourseCancelRequest;
import org.hyejoon.cuvcourse.domain.course.cousecancel.service.CourseCancelService;
import org.hyejoon.cuvcourse.global.auth.AuthConstant;
import org.hyejoon.cuvcourse.global.dto.GlobalResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CourseCancelController {

    private final CourseCancelService courseCancelService;

    @DeleteMapping("/api/courses")
    public GlobalResponse<?> deleteCourse(
        @Valid @RequestBody CourseCancelRequest courseCancleRequest,
        @RequestHeader(AuthConstant.X_STUDENT_ID) Long studentId
    ) {
        courseCancelService.courseCancel(courseCancleRequest.lectureId(), studentId);

        return GlobalResponse.ok("수강 신청이 취소되었습니다.", null);
    }
}
