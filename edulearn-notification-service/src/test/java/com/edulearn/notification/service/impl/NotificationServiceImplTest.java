package com.edulearn.notification.service.impl;

import com.edulearn.notification.client.AuthServiceClient;
import com.edulearn.notification.dto.NotificationEvent;
import com.edulearn.notification.dto.UserResponse;
import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.repository.NotificationRepository;
import com.edulearn.notification.service.WhatsAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private WhatsAppService whatsappService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationEvent event;

    @BeforeEach
    void setUp() {
        event = NotificationEvent.builder()
                .userId("user123")
                .eventType("ENROLLMENT")
                .title("Enrolled!")
                .message("Welcome to the course")
                .build();
    }

    @Test
    void createNotification_shouldSaveAndTriggerWhatsApp() {
        UserResponse user = new UserResponse();
        user.setMobile("1234567890");
        when(authServiceClient.getUserById("user123")).thenReturn(user);

        notificationService.createNotification(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(whatsappService).sendWhatsAppMessage(eq("1234567890"), anyString());
    }

    @Test
    void markAsRead_shouldUpdateStatus() {
        UUID id = UUID.randomUUID();
        Notification n = new Notification();
        n.setIsRead(false);
        when(notificationRepository.findById(id)).thenReturn(Optional.of(n));

        notificationService.markAsRead(id);

        assertTrue(n.getIsRead());
        verify(notificationRepository).save(n);
    }

    private void assertTrue(Boolean condition) {
        if (!condition) throw new AssertionError();
    }
}
