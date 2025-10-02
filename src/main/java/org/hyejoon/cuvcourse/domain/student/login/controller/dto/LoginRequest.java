package org.hyejoon.cuvcourse.domain.student.login.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email, @NotBlank String password) {
}
