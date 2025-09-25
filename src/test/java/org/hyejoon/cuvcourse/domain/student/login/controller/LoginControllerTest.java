package org.hyejoon.cuvcourse.domain.student.login.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.hyejoon.cuvcourse.domain.student.login.controller.dto.LoginRequest;
import org.hyejoon.cuvcourse.domain.student.login.exception.LoginExceptionEnum;
import org.hyejoon.cuvcourse.domain.student.login.service.LoginService;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(LoginController.class)
public class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginService loginService;

    @Test
    void 로그인_존재하지_않는_이메일은_실패한다() throws JsonProcessingException, Exception {
        LoginRequest loginRequest = new LoginRequest("not-email", "password");

        willThrow(new BusinessException(LoginExceptionEnum.LOGIN_FAILED)).given(loginService)
                .login(anyString(), anyString());

        mockMvc.perform(post("/api/students/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인_일치하지_않는_패스워드는_실패한다() throws JsonProcessingException, Exception {
        LoginRequest loginRequest = new LoginRequest("email", "wrong-password");

        willThrow(new BusinessException(LoginExceptionEnum.LOGIN_FAILED)).given(loginService)
                .login(anyString(), anyString());

        mockMvc.perform(post("/api/students/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인_성공한다() throws JsonProcessingException, Exception {
        LoginRequest loginRequest = new LoginRequest("email", "password");

        given(loginService.login(anyString(), anyString())).willReturn(1L);

        mockMvc.perform(post("/api/students/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(1L));
    }
}
