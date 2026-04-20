package com.edulearn.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduLearn Auth Service API")
                        .version("1.0")
                        .description("""
                                Authentication and authorization service for EduLearn.
                                
                                Includes:
                                - Registration
                                - Login
                                - Current user
                                - Forgot password
                                - Reset password
                                - Admin instructor approval APIs
                                - Google OAuth login
                                """)
                        .contact(new Contact()
                                .name("EduLearn Team")
                                .email("support@edulearn.com"))
                        .license(new License()
                                .name("Internal Use")));
    }
}