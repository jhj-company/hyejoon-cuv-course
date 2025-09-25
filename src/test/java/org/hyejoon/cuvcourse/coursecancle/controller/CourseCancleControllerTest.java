package org.hyejoon.cuvcourse.coursecancle.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hyejoon.cuvcourse.domain.course.cousecancle.controller.CourseCancleController;
import org.hyejoon.cuvcourse.domain.course.cousecancle.exception.CourseCancleExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.cousecancle.service.CourseCancleService;
import org.hyejoon.cuvcourse.global.auth.AuthConstant;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseCancleController.class)
public class CourseCancleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseCancleService courseCancleService;

    @Test
    void 수강취소_성공() throws Exception {
        // given
        long lectureId = 1L;
        long studentId = 1L;

        doNothing().when(courseCancleService).courseCancle(lectureId, studentId);

        // when & then
        mockMvc.perform(delete("/api/courses/{lectureId}", lectureId)
                .header(AuthConstant.X_STUDENT_ID, studentId))
            .andExpect(status().isOk());
    }

    @Test
    void 수강취소_수강신청없으면_실패() throws Exception {
        // given
        long lectureId = 9999L;
        long studentId = 1L;

        doThrow(new BusinessException(CourseCancleExceptionEnum.COURSE_NOT_FOUND))
            .when(courseCancleService).courseCancle(lectureId, studentId);

        mockMvc.perform(delete("/api/courses/{lectureId}", lectureId)
                .header(AuthConstant.X_STUDENT_ID, studentId))
            .andExpect(status().isBadRequest());
    }
}
