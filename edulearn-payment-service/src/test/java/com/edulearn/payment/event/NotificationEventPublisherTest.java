package com.edulearn.payment.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventPublisher Unit Tests")
class NotificationEventPublisherTest {

    @Mock
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @InjectMocks
    private NotificationEventPublisher publisher;

    @Test
    void publish_shouldSendToKafka() {

        NotificationEvent event = NotificationEvent.builder()
                .title("T")
                .build();

        publisher.publish(event);

        verify(kafkaTemplate).send(
                anyString(),
                isNull(),
                eq(event)
        );
    }
}