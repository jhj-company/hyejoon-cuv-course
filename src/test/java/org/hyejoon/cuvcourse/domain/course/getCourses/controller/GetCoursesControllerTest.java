package org.hyejoon.cuvcourse.domain.course.getCourses.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.hyejoon.cuvcourse.domain.course.getCourses.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.getCourses.dto.GetCoursesResponse;
import org.hyejoon.cuvcourse.domain.course.getCourses.service.GetCoursesService;
import org.hyejoon.cuvcourse.global.auth.AuthConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GetCoursesController.class)
public class GetCoursesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCoursesService getCoursesService;

    @Test
    @DisplayName("수강 목록 조회 성공 - 수강 내역이 있는 경우")
    void 수강신청_목록조회에_성공한다() throws Exception {
        // given
        Long studentId = 1L;

        List<CourseResponse> courses = List.of(
            new CourseResponse(1L, "lecture1", "professor", 3),
            new CourseResponse(2L, "lecture2", "professor2", 3)
        );

        GetCoursesResponse response = new GetCoursesResponse(courses, 6, 18);

        when(getCoursesService.getCourses(studentId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/courses")
                .header(AuthConstant.X_STUDENT_ID, studentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courses[0].lectureId").value(1))
            .andExpect(jsonPath("$.data.courses[0].lectureTitle").value("lecture1"))
            .andExpect(jsonPath("$.data.enrolledCredits").value(6))
            .andExpect(jsonPath("$.data.availableCredits").value(18));
    }

    @Test
    @DisplayName("수강 목록 조회 성공 - 수강 내역이 없는 경우")
    void getCourses_success_emptyCourses() throws Exception {
        // given
        Long studentId = 2L;

        GetCoursesResponse response = new GetCoursesResponse(List.of(), 0, 18);

        when(getCoursesService.getCourses(studentId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/courses")
                .header(AuthConstant.X_STUDENT_ID, studentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courses").isEmpty())
            .andExpect(jsonPath("$.data.enrolledCredits").value(0))
            .andExpect(jsonPath("$.data.availableCredits").value(18));
    }
}
