package com.edulearn.notification.service.impl;

import com.edulearn.notification.client.AuthServiceClient;
import com.edulearn.notification.dto.NotificationEvent;
import com.edulearn.notification.dto.NotificationResponse;
import com.edulearn.notification.dto.UserResponse;
import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.repository.NotificationRepository;
import com.edulearn.notification.service.NotificationService;
import com.edulearn.notification.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthServiceClient authServiceClient;
    private final WhatsAppService whatsappService;

    @Override
    public void createNotification(NotificationEvent event) {
        log.info("Creating notification for userId={}, type={}", event.getUserId(), event.getEventType());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .title(event.getTitle())
                .message(event.getMessage())
                .eventType(event.getEventType())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notification saved for userId={}", event.getUserId());

        // WhatsApp Notification logic for specific events
        triggerWhatsAppNotification(event);
    }

    private void triggerWhatsAppNotification(NotificationEvent event) {
        String type = event.getEventType();
        if ("USER_REGISTERED".equals(type) || "ENROLLMENT".equals(type) || "PAYMENT_SUCCESS".equals(type) || "CERTIFICATION".equals(type)) {
            try {
                UserResponse user = authServiceClient.getUserById(event.getUserId());
                if (user != null && user.getMobile() != null && !user.getMobile().isEmpty()) {
                    String whatsappBody = String.format("*EduLearn Alert*\n\n%s\n\n%s", event.getTitle(), event.getMessage());
                    whatsappService.sendWhatsAppMessage(user.getMobile(), whatsappBody);
                } else {
                    log.warn("Could not send WhatsApp message: User mobile number not found for userId={}", event.getUserId());
                }
            } catch (Exception e) {
                log.error("Error fetching user details for WhatsApp notification: {}", e.getMessage());
            }
        }
    }

    @Override
    public List<NotificationResponse> getNotificationsByUser(String userId) {
        log.debug("Fetching all notifications for userId={}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getUnreadByUser(String userId) {
        log.debug("Fetching unread notifications for userId={}", userId);
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(UUID notificationId) {
        log.info("Marking notification {} as read", notificationId);
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        log.info("Marking all notifications as read for userId={}", userId);
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .eventType(notification.getEventType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
