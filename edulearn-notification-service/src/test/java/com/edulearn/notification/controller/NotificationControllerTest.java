package com.edulearn.notification.controller;

import com.edulearn.notification.dto.NotificationResponse;
import com.edulearn.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void getNotifications_shouldReturnOk() throws Exception {
        when(notificationService.getNotificationsByUser("user-123")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/user/user-123"))
                .andExpect(status().isOk());
    }

    @Test
    void getUnread_shouldReturnOk() throws Exception {
        when(notificationService.getUnreadByUser("user-123")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/user/user-123/unread"))
                .andExpect(status().isOk());
    }

    @Test
    void getUnreadCount_shouldReturnCount() throws Exception {
        when(notificationService.getUnreadCount("user-123")).thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications/user/user-123/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void markAsRead_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/notifications/{id}/read", id))
                .andExpect(status().isOk());
    }

    @Test
    void markAllAsRead_shouldReturnOk() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/user/user-123/read-all"))
                .andExpect(status().isOk());
    }

    @Test
    void ping_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Notification service is working"));
    }
}
