package com.edulearn.notification.service;

import com.edulearn.notification.dto.NotificationEvent;
import com.edulearn.notification.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void createNotification(NotificationEvent event);
    List<NotificationResponse> getNotificationsByUser(String userId);
    List<NotificationResponse> getUnreadByUser(String userId);
    long getUnreadCount(String userId);
    void markAsRead(UUID notificationId);
    void markAllAsRead(String userId);
}
