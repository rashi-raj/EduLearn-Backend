package com.edulearn.course.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI courseServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduLearn Course Service API")
                        .description("Handles course creation, approval, publishing")
                        .version("1.0"));
    }
}