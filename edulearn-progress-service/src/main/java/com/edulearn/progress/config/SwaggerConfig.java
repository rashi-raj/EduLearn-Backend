package com.edulearn.progress.config;

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

    @Value("${server.port:8086}")
    private String serverPort;

    @Bean
    public OpenAPI progressServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduLearn Progress Service API")
                        .version("v1.0")
                        .description("Progress service APIs for tracking lesson completion, course progress, and certificate generation.")
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
