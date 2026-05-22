package com.edulearn.notification.client;

import com.edulearn.notification.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "EDULEARN-AUTH-SERVICE", path = "/api/v1/auth")
public interface AuthServiceClient {

    @GetMapping("/internal/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") String userId);
}
