package com.edulearn.notification.consumer;

import com.edulearn.notification.dto.NotificationEvent;
import com.edulearn.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = {
            "edulearn.user.events",
            "edulearn.course.events",
            "edulearn.enrollment.events",
            "edulearn.assessment.events",
            "edulearn.payment.events",
            "edulearn.progress.events"
        }, 
        groupId = "notification-group", 
        concurrency = "1"
    )
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: type={}, userId={}, title={}", 
            event.getEventType(), event.getUserId(), event.getTitle());
        notificationService.createNotification(event);
    }

    @KafkaListener(topics = "edulearn.progress.events", groupId = "notification-group", concurrency = "1")
    public void handleProgressEvent(NotificationEvent event) {
        log.info("Received progress event: type={}, userId={}", event.getEventType(), event.getUserId());
        notificationService.createNotification(event);
    }

}
