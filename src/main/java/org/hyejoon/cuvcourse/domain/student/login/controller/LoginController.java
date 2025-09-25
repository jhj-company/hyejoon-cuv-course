package org.hyejoon.cuvcourse.domain.student.login.controller;

import org.hyejoon.cuvcourse.domain.student.login.controller.dto.LoginRequest;
import org.hyejoon.cuvcourse.domain.student.login.controller.dto.LoginResponse;
import org.hyejoon.cuvcourse.domain.student.login.service.LoginService;
import org.hyejoon.cuvcourse.global.dto.GlobalResponse;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/api/students/login")
    public GlobalResponse<?> login(@RequestBody LoginRequest request) {
        long studentId = loginService.login(request.email(), request.password());
        LoginResponse response = new LoginResponse(studentId);
        return GlobalResponse.ok("로그인 성공", response);
    }

}
