package org.hyejoon.cuvcourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CuvcourseApplication {

	public static void main(String[] args) {
		SpringApplication.run(CuvcourseApplication.class, args);
	}

}
