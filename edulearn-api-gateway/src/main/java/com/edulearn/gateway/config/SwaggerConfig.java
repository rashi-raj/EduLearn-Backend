package com.edulearn.gateway.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI eduLearnGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduLearn API Gateway")
                        .version("1.0")
                        .description("""
                                Central entry point for EduLearn microservices.

                                Routed services:
                                - Auth Service
                                - Course Service
                                - Lesson Service
                                - Enrollment Service
                                - Assessment Service
                                - Progress Service
                                - Payment Service
                                """)
                        .contact(new Contact()
                                .name("EduLearn Team")
                                .email("support@edulearn.com"))
                        .license(new License()
                                .name("Internal Use")))
                .externalDocs(new ExternalDocumentation()
                        .description("EduLearn Microservices Documentation"));
    }
}