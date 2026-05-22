package com.edulearn.notification.consumer;

import com.edulearn.notification.dto.NotificationEvent;
import com.edulearn.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationConsumer Unit Tests")
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void handleNotificationEvent_shouldCallService() {
        NotificationEvent event = new NotificationEvent();
        notificationConsumer.handleNotificationEvent(event);
        verify(notificationService).createNotification(event);
    }
}
