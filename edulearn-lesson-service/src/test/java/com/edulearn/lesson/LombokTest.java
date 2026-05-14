package com.edulearn.lesson;

import com.edulearn.lesson.dto.*;
import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lombok-generated Methods Coverage Test")
class LombokTest {

    @Test
    @DisplayName("Verify DTOs and Entities coverage")
    void testDataClasses() {
        // Lesson Entity
        Lesson lesson = new Lesson();
        UUID id = UUID.randomUUID();
        lesson.setLessonId(id);
        lesson.setTitle("T");
        assertEquals(id, lesson.getLessonId());
        assertEquals("T", lesson.getTitle());
        assertNotNull(lesson.toString());

        Lesson lesson2 = Lesson.builder().title("B").build();
        assertEquals("B", lesson2.getTitle());

        // Resource Entity
        Resource resource = new Resource();
        resource.setName("R");
        assertEquals("R", resource.getName());
        assertNotNull(resource.toString());

        Resource resource2 = Resource.builder().name("B").build();
        assertEquals("B", resource2.getName());

        // DTOs
        LessonRequest req = new LessonRequest();
        req.setTitle("R");
        assertEquals("R", req.getTitle());

        LessonResponse resp = LessonResponse.builder()
                .title("S")
                .resources(Collections.emptyList())
                .build();
        assertEquals("S", resp.getTitle());

        LessonOrderUpdateRequest orderReq = new LessonOrderUpdateRequest();
        orderReq.setOrderIndex(1);
        assertEquals(1, orderReq.getOrderIndex());

        ResourceRequest resReq = new ResourceRequest();
        resReq.setName("Res");
        assertEquals("Res", resReq.getName());

        ResourceResponse resResp = ResourceResponse.builder().name("Res").build();
        assertEquals("Res", resResp.getName());
    }
}
