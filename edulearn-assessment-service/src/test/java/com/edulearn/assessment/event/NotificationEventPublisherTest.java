package com.edulearn.assessment.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventPublisher Unit Tests")
class NotificationEventPublisherTest {

    @Mock
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @InjectMocks
    private NotificationEventPublisher publisher;

    @Test
    @DisplayName("publish: sends event to Kafka")
    void publish_shouldSendEvent() {
        NotificationEvent event = NotificationEvent.builder()
                .eventType("TEST")
                .userId("user123")
                .build();

        publisher.publish(event);

        verify(kafkaTemplate).send(eq("edulearn.assessment.events"), eq("user123"), any());
    }
}
