package com.edulearn.notification;

import com.edulearn.notification.dto.NotificationEvent;
import com.edulearn.notification.dto.NotificationResponse;
import com.edulearn.notification.dto.UserResponse;
import com.edulearn.notification.entity.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Lombok-generated Methods Coverage Test")
class LombokTest {

    @Test
    void testDataClasses() {
        // Notification Entity
        Notification n = new Notification();
        n.setNotificationId(UUID.randomUUID());
        n.setTitle("T");
        n.setMessage("M");
        n.setUserId("U");
        n.setIsRead(true);
        n.setCreatedAt(LocalDateTime.now());
        
        assertEquals("T", n.getTitle());
        assertEquals("M", n.getMessage());
        assertEquals("U", n.getUserId());
        assertNotNull(n.toString());

        Notification n2 = Notification.builder().title("B").build();
        assertEquals("B", n2.getTitle());

        // DTOs
        NotificationEvent event = NotificationEvent.builder().title("E").build();
        assertEquals("E", event.getTitle());

        NotificationResponse resp = NotificationResponse.builder().title("R").build();
        assertEquals("R", resp.getTitle());

        UserResponse user = new UserResponse();
        user.setMobile("123");
        assertEquals("123", user.getMobile());
    }
}
