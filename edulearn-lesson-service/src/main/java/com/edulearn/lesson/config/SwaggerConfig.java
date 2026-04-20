package com.edulearn.lesson.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI lessonServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduLearn Lesson Service API")
                        .description("Handles lessons and resources inside courses")
                        .version("1.0"));
    }
}