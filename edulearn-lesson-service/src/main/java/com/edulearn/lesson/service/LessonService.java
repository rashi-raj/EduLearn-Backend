package com.edulearn.lesson.service;

import com.edulearn.lesson.dto.LessonOrderUpdateRequest;
import com.edulearn.lesson.dto.LessonRequest;
import com.edulearn.lesson.dto.LessonResponse;
import com.edulearn.lesson.dto.ResourceRequest;
import com.edulearn.lesson.dto.ResourceResponse;

import java.util.List;
import java.util.UUID;

public interface LessonService {

    LessonResponse addLesson(LessonRequest request);

    List<LessonResponse> getLessonsByCourse(UUID courseId);

    LessonResponse getLessonById(UUID lessonId);

    LessonResponse updateLesson(UUID lessonId, LessonRequest request);

    void deleteLesson(UUID lessonId);

    List<LessonResponse> reorderLessons(UUID courseId, List<LessonOrderUpdateRequest> requests);

    ResourceResponse addResource(UUID lessonId, ResourceRequest request);

    void removeResource(UUID resourceId);

    List<LessonResponse> getPreviewLessons(UUID courseId);
}