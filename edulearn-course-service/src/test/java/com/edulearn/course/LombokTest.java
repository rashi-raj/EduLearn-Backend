package com.edulearn.course;

import com.edulearn.course.dto.*;
import com.edulearn.course.entity.Course;
import com.edulearn.course.entity.CourseStatus;
import com.edulearn.course.event.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lombok-generated Methods Coverage Test")
class LombokTest {

    @Test
    @DisplayName("Verify DTOs and Entities coverage")
    void testDataClasses() {
        // Course Entity
        Course course = new Course();
        UUID id = UUID.randomUUID();
        UUID instId = UUID.randomUUID();
        course.setCourseId(id);
        course.setTitle("Title");
        course.setDescription("Desc");
        course.setCategory("Cat");
        course.setLevel("Level");
        course.setPrice(BigDecimal.TEN);
        course.setInstructorId(instId);
        course.setThumbnailUrl("thumb");
        course.setTotalDuration(10);
        course.setIsPublished(true);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setLanguage("English");
        course.setValidityInMonths(12);
        course.setCreatedAt(LocalDateTime.now());

        assertEquals(id, course.getCourseId());
        assertEquals("Title", course.getTitle());
        assertEquals("Desc", course.getDescription());
        assertEquals("Cat", course.getCategory());
        assertEquals("Level", course.getLevel());
        assertEquals(BigDecimal.TEN, course.getPrice());
        assertEquals(instId, course.getInstructorId());
        assertEquals("thumb", course.getThumbnailUrl());
        assertEquals(10, course.getTotalDuration());
        assertTrue(course.getIsPublished());
        assertEquals(CourseStatus.PUBLISHED, course.getStatus());
        assertEquals("English", course.getLanguage());
        assertEquals(12, course.getValidityInMonths());
        assertNotNull(course.getCreatedAt());
        assertNotNull(course.toString());

        Course course2 = Course.builder().title("B").build();
        assertEquals("B", course2.getTitle());

        // DTOs
        CourseRequest req = new CourseRequest();
        req.setTitle("T");
        assertEquals("T", req.getTitle());

        CourseResponse resp = CourseResponse.builder().title("R").build();
        assertEquals("R", resp.getTitle());

        CourseApprovalActionRequest approveReq = new CourseApprovalActionRequest();
        approveReq.setAction("APPROVE");
        assertEquals("APPROVE", approveReq.getAction());

        PublishCourseRequest pubReq = new PublishCourseRequest();
        pubReq.setPublished(true);
        assertTrue(pubReq.getPublished());

        // Events
        NotificationEvent event = NotificationEvent.builder()
                .eventType("E")
                .userId("U")
                .title("T")
                .message("M")
                .build();
        assertEquals("E", event.getEventType());
        assertNotNull(event.toString());
        
        NotificationEvent event2 = new NotificationEvent();
        event2.setEventType("E2");
        assertEquals("E2", event2.getEventType());
        
        NotificationEvent event3 = new NotificationEvent("E3", "U3", "T3", "M3");
        assertEquals("E3", event3.getEventType());
    }
}
