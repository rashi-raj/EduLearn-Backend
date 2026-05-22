package com.edulearn.notification.controller;

import com.edulearn.notification.dto.NotificationResponse;
import com.edulearn.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Controller", description = "APIs for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable String userId) {
        log.debug("Fetching notifications for userId={}", userId);
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @Operation(summary = "Get unread notifications for a user")
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(@PathVariable String userId) {
        log.debug("Fetching unread notifications for userId={}", userId);
        return ResponseEntity.ok(notificationService.getUnreadByUser(userId));
    }

    @Operation(summary = "Get unread notification count")
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String userId) {
        log.debug("Getting unread count for userId={}", userId);
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @Operation(summary = "Mark a notification as read")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        log.info("Marking notification {} as read", notificationId);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark all notifications as read for a user")
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        log.info("Marking all notifications as read for userId={}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Health check for notification service")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.debug("Ping endpoint invoked");
        return ResponseEntity.ok("Notification service is working");
    }
}
