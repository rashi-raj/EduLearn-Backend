package com.edulearn.assessment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8085}")
    private String serverPort;

    @Bean
    public OpenAPI assessmentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduLearn Assessment Service API")
                        .version("v1.0")
                        .description("Assessment service APIs for quiz management, question management, and student quiz attempts.")
                        .contact(new Contact()
                                .name("EduLearn Team")
                                .email("support@edulearn.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local environment")
                ));
    }
}
