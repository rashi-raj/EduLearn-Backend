package com.edulearn.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private static final String TOPIC = "edulearn.user.events";

    public void publish(NotificationEvent event) {
        log.info("Publishing event to {}: type={}, userId={}", TOPIC, event.getEventType(), event.getUserId());
        kafkaTemplate.send(TOPIC, event.getUserId(), event);
    }
}
