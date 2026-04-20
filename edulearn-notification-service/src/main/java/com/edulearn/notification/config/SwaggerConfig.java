package com.edulearn.notification.config;

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

    @Value("${server.port:8088}")
    private String serverPort;

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduLearn Notification Service API")
                        .version("v1.0")
                        .description("Notification service APIs for managing user notifications driven by Kafka events.")
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
