package org.hyejoon.cuvcourse.domain.student.login.service;

import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.login.exception.LoginExceptionEnum;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final StudentJpaRepository studentRepository;

    @Transactional(readOnly = true)
    public long login(String email, String password) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(LoginExceptionEnum.LOGIN_FAILED));
        if (BCrypt.checkpw(password, student.getPassword()) == false) {
            throw new BusinessException(LoginExceptionEnum.LOGIN_FAILED);
        }
        return student.getId();
    }
}
