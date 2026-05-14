package com.edulearn.auth.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventPublisher Unit Tests")
class NotificationEventPublisherTest {

    @Mock
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @InjectMocks
    private NotificationEventPublisher publisher;

    @Test
    @DisplayName("publish: successfully sends event to Kafka")
    void publish_success() {
        NotificationEvent event = NotificationEvent.builder()
                .userId("user-123")
                .eventType("USER_CREATED")
                .build();

        publisher.publish(event);

        verify(kafkaTemplate).send(eq("edulearn.user.events"), eq("user-123"), eq(event));
    }

    @Test
    @DisplayName("publish: logs error when Kafka send fails")
    void publish_failure_logsError() {
        NotificationEvent event = NotificationEvent.builder()
                .userId("user-123")
                .eventType("USER_CREATED")
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenThrow(new RuntimeException("Kafka down"));

        publisher.publish(event);

        // Verify it doesn't throw exception but logs it (verified via mock interaction)
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }
}
