package org.hyejoon.cuvcourse.domain.course.getCourses.controller;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.getCourses.dto.GetCoursesResponse;
import org.hyejoon.cuvcourse.domain.course.getCourses.service.GetCoursesService;
import org.hyejoon.cuvcourse.global.auth.AuthConstant;
import org.hyejoon.cuvcourse.global.dto.GlobalResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GetCoursesController {

    private final GetCoursesService getCoursesService;

    @GetMapping("/courses")
    public GlobalResponse<?> getCourses(@RequestHeader(AuthConstant.X_STUDENT_ID) Long studentId) {
        GetCoursesResponse response = getCoursesService.getCourses(studentId);
        return GlobalResponse.ok("수강신청 목록을 조회했습니다.", response);
    }
}
