package org.hyejoon.cuvcourse.domain.course.courseregist.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseRegistRequest;
import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseRegistService;
import org.hyejoon.cuvcourse.global.auth.AuthConstant;
import org.hyejoon.cuvcourse.global.dto.GlobalResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRegistController {

    private final CourseRegistService courseRegistService;

    @PostMapping("/api/courses")
    public GlobalResponse<CourseResponse> registCourse(
        @Valid @RequestBody CourseRegistRequest request,
        @RequestHeader(AuthConstant.X_STUDENT_ID) Long studentId
    ) {
        CourseResponse courseResponse = courseRegistService.registerCourse(studentId,
            request.lectureId());
        return GlobalResponse.ok("수강신청이 성공적으로 완료되었습니다.", courseResponse);
    }
}
