package com.edulearn.registry.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegistryStartupLogger {

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupInfo() {
        log.info("===============================================");
        log.info("EduLearn Service Registry started successfully");
        log.info("Eureka Dashboard: http://localhost:8761");
        log.info("Actuator Health: http://localhost:8761/actuator/health");
        log.info("===============================================");
    }
}