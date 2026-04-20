package com.edulearn.notification.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID notificationId;
    private String userId;
    private String title;
    private String message;
    private String eventType;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
