package com.edulearn.notification.service;

import com.edulearn.notification.client.AuthServiceClient;
import com.edulearn.notification.dto.NotificationEvent;
import com.edulearn.notification.dto.NotificationResponse;
import com.edulearn.notification.dto.UserResponse;
import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.repository.NotificationRepository;
import com.edulearn.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Unit Tests")
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
    private Notification notification;

    @BeforeEach
    void setUp() {
        event = NotificationEvent.builder()
                .userId("user-123")
                .title("Welcome")
                .message("Hello User")
                .eventType("USER_REGISTERED")
                .build();

        notification = Notification.builder()
                .notificationId(UUID.randomUUID())
                .userId("user-123")
                .title("Welcome")
                .isRead(false)
                .build();
    }

    @Test
    void createNotification_shouldSaveAndTriggerWhatsApp() {
        UserResponse user = new UserResponse();
        user.setMobile("9876543210");
        
        when(authServiceClient.getUserById("user-123")).thenReturn(user);

        notificationService.createNotification(event);

        verify(notificationRepository).save(any());
        verify(whatsappService).sendWhatsAppMessage(eq("9876543210"), anyString());
    }

    @Test
    void createNotification_shouldNotTriggerWhatsApp_whenMobileMissing() {
        UserResponse user = new UserResponse();
        when(authServiceClient.getUserById("user-123")).thenReturn(user);

        notificationService.createNotification(event);

        verify(notificationRepository).save(any());
        verify(whatsappService, never()).sendWhatsAppMessage(anyString(), anyString());
    }

    @Test
    void getNotificationsByUser_shouldReturnList() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user-123"))
                .thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getNotificationsByUser("user-123");

        assertEquals(1, responses.size());
    }

    @Test
    void getUnreadByUser_shouldReturnList() {
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc("user-123"))
                .thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getUnreadByUser("user-123");

        assertEquals(1, responses.size());
    }

    @Test
    void getUnreadCount_shouldReturnCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse("user-123")).thenReturn(5L);

        long count = notificationService.getUnreadCount("user-123");

        assertEquals(5L, count);
    }

    @Test
    void markAsRead_shouldUpdateStatus() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(id);

        assertTrue(notification.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAllAsRead_shouldUpdateAll() {
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc("user-123"))
                .thenReturn(List.of(notification));

        notificationService.markAllAsRead("user-123");

        assertTrue(notification.getIsRead());
        verify(notificationRepository).saveAll(any());
    }
}
