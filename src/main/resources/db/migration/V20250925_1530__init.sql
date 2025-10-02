CREATE TABLE students (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '학생 이름',
    email VARCHAR(255) NOT NULL COMMENT '로그인 이메일',
    password VARCHAR(255) NOT NULL COMMENT '로그인 패스워드',
    available_credits INT UNSIGNED NOT NULL COMMENT '신청 가능한 학점',
    created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_students_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='학생 테이블';

CREATE TABLE lectures (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lecture_title VARCHAR(255) NOT NULL COMMENT '강의명',
    professor_name VARCHAR(255) NOT NULL COMMENT '교수명',
    credits INT UNSIGNED NOT NULL COMMENT '학점',
    capacity INT UNSIGNED NOT NULL COMMENT '정원',
    created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='강의 테이블';

CREATE TABLE courses (
    lecture_id BIGINT NOT NULL COMMENT '강의 외래키',
    student_id BIGINT NOT NULL COMMENT '학생 외래키',
    created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (lecture_id, student_id),
    FOREIGN KEY (lecture_id) REFERENCES lectures(id),
    FOREIGN KEY (student_id) REFERENCES students(id),

    KEY idx_courses_lecture (lecture_id),
    KEY idx_courses_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='수강신청 테이블';
