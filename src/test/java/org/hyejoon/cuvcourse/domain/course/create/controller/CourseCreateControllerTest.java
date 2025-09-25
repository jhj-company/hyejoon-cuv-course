package org.hyejoon.cuvcourse.domain.course.create.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.create.service.CourseCreateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseCreateController.class)
class CourseCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CourseCreateService courseCreateService;

    @Test
    @DisplayName("성공 : 학생이 정상적으로 강의를 수강신청한다")
    void 수강신청_성공() throws Exception {
        //given
        long studentId = 1L;
        long lectureId = 10L;
        CourseResponse response = new CourseResponse(studentId, lectureId, LocalDateTime.now());

        given(courseCreateService.createCourse(studentId, lectureId))
            .willReturn(response);

        //when & then
        mockMvc.perform(post("/api/courses")
                .header("X-Student-Id", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lectureId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("수강신청이 성공적으로 완료되었습니다."))
            .andExpect(jsonPath("$.data.studentId").value(studentId))
            .andExpect(jsonPath("$.data.lectureId").value(lectureId));
    }

    @Test
    @DisplayName("실패: 이미 신청한 강의를 중복으로 신청한다")
    void 수강신청_실패_중복신청() throws Exception {
        long studentId = 1L;
        long lectureId = 101L;

        doThrow(new IllegalStateException("이미 수강신청한 강의입니다."))
            .when(courseCreateService)
            .createCourse(anyLong(), anyLong());

        mockMvc.perform(post("/api/courses")
                .header("X-Student-Id", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lectureId)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 수강신청한 강의입니다."));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public CourseCreateService courseCreateService() {
            return Mockito.mock(CourseCreateService.class);
        }
    }
}
