package org.hyejoon.cuvcourse.domain.course.courseregist.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseRegistRequest;
import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.courseregist.service.CourseRegistService;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseRegistController.class)
class CourseRegistControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CourseRegistService courseRegistService;

    @Test
    @DisplayName("성공 : 학생이 정상적으로 강의를 수강신청한다")
    void 수강신청_성공() throws Exception {
        //given
        long studentId = 1L;
        long lectureId = 10L;
        CourseResponse response = new CourseResponse(studentId, lectureId, LocalDateTime.now());

        given(courseRegistService.registerCourse(studentId, lectureId))
            .willReturn(response);

        String requestBody = objectMapper.writeValueAsString(
            new CourseRegistRequest(lectureId)
        );

        //when & then
        mockMvc.perform(post("/api/courses")
            .header("X-Student-Id", studentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
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

        String requestBody = objectMapper.writeValueAsString(
            new CourseRegistRequest(lectureId)
        );

        doThrow(new BusinessException(CourseRegistExceptionEnum.ALREADY_REGISTERED))
            .when(courseRegistService)
            .registerCourse(anyLong(), anyLong());

        mockMvc.perform(post("/api/courses")
            .header("X-Student-Id", studentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            // 💡 BusinessException이 409 Conflict로 처리된다고 가정하고 검증
            .andExpect(status().isConflict())
            // 💡 응답 메시지도 BusinessException에서 가져온 메시지와 일치하도록 검증
            .andExpect(
                jsonPath("$.message").value(CourseRegistExceptionEnum.ALREADY_REGISTERED
                    .getMessage()));
    }
}
