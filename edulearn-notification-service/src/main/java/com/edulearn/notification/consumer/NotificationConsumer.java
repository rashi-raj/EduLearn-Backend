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

    @KafkaListener(topics = "edulearn.user.events", groupId = "notification-group")
    public void handleUserEvent(NotificationEvent event) {
        log.info("Received user event: type={}, userId={}", event.getEventType(), event.getUserId());
        notificationService.createNotification(event);
    }

    @KafkaListener(topics = "edulearn.course.events", groupId = "notification-group")
    public void handleCourseEvent(NotificationEvent event) {
        log.info("Received course event: type={}, userId={}", event.getEventType(), event.getUserId());
        notificationService.createNotification(event);
    }

    @KafkaListener(topics = "edulearn.enrollment.events", groupId = "notification-group")
    public void handleEnrollmentEvent(NotificationEvent event) {
        log.info("Received enrollment event: type={}, userId={}", event.getEventType(), event.getUserId());
        notificationService.createNotification(event);
    }

    @KafkaListener(topics = "edulearn.assessment.events", groupId = "notification-group")
    public void handleAssessmentEvent(NotificationEvent event) {
        log.info("Received assessment event: type={}, userId={}", event.getEventType(), event.getUserId());
        notificationService.createNotification(event);
    }

    @KafkaListener(topics = "edulearn.payment.events", groupId = "notification-group")
    public void handlePaymentEvent(NotificationEvent event) {
        log.info("Received payment event: type={}, userId={}", event.getEventType(), event.getUserId());
        notificationService.createNotification(event);
    }
}
