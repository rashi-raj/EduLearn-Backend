# Enrollment Service Updates

## Added
- Structured logging using `@Slf4j` in controller, service, and exception handler.
- Swagger/OpenAPI configuration via `springdoc-openapi-starter-webmvc-ui`.
- SwaggerConfig class with API metadata.
- OpenAPI annotations on controller endpoints.
- Mockito-based unit tests for service layer.

## Updated
- `pom.xml`
  - aligned Spring Boot to `3.2.5`
  - aligned Spring Cloud to `2023.0.3`
  - replaced unusual test starters with `spring-boot-starter-test`
  - added `springdoc-openapi-starter-webmvc-ui:2.5.0`
- `application.yml`
  - added `springdoc` paths
  - added logging levels/pattern
- `EnrollmentController.java`
- `EnrollmentServiceImpl.java`
- `GlobalExceptionHandler.java`

## New Files
- `src/main/java/com/edulearn/enrollment/config/SwaggerConfig.java`
- `src/test/java/com/edulearn/enrollment/service/EnrollmentServiceImplTest.java`

## Swagger URL
- `http://localhost:8084/swagger-ui.html`
- `http://localhost:8084/v3/api-docs`

## Note
Build/test verification could not be completed inside this container because Maven wrapper needs to download Maven from the internet, which is blocked here.
