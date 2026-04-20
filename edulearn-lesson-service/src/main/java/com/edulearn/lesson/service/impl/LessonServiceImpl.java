package com.edulearn.lesson.service.impl;

import com.edulearn.lesson.dto.LessonOrderUpdateRequest;
import com.edulearn.lesson.dto.LessonRequest;
import com.edulearn.lesson.dto.LessonResponse;
import com.edulearn.lesson.dto.ResourceRequest;
import com.edulearn.lesson.dto.ResourceResponse;
import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.entity.Resource;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.repository.ResourceRepository;
import com.edulearn.lesson.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private static final Logger log = LoggerFactory.getLogger(LessonServiceImpl.class);

    private final LessonRepository lessonRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public LessonResponse addLesson(LessonRequest request) {
        log.info("Creating lesson: {}", request.getTitle());

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .courseId(request.getCourseId())
                .orderIndex(request.getOrderIndex())
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);

        log.info("Lesson created successfully with ID: {}", savedLesson.getLessonId());

        return mapToResponse(savedLesson);
    }

    @Override
    public List<LessonResponse> getLessonsByCourse(UUID courseId) {
        log.info("Fetching lessons for course ID: {}", courseId);

        List<LessonResponse> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} lessons for course ID: {}", lessons.size(), courseId);
        return lessons;
    }

    @Override
    public LessonResponse getLessonById(UUID lessonId) {
        log.info("Fetching lesson by ID: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> {
                    log.error("Lesson not found with ID: {}", lessonId);
                    return new RuntimeException("Lesson not found");
                });

        log.info("Lesson fetched successfully with ID: {}", lessonId);
        return mapToResponse(lesson);
    }

    @Override
    public LessonResponse updateLesson(UUID lessonId, LessonRequest request) {
        log.info("Updating lesson with ID: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> {
                    log.error("Lesson not found with ID: {}", lessonId);
                    return new RuntimeException("Lesson not found");
                });

        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setOrderIndex(request.getOrderIndex());

        Lesson updatedLesson = lessonRepository.save(lesson);

        log.info("Lesson updated successfully with ID: {}", lessonId);

        return mapToResponse(updatedLesson);
    }

    @Override
    public void deleteLesson(UUID lessonId) {
        log.info("Deleting lesson with ID: {}", lessonId);

        if (!lessonRepository.existsById(lessonId)) {
            log.error("Lesson not found for deletion: {}", lessonId);
            throw new RuntimeException("Lesson not found");
        }

        lessonRepository.deleteById(lessonId);

        log.info("Lesson deleted successfully with ID: {}", lessonId);
    }

    @Override
    public List<LessonResponse> reorderLessons(UUID courseId, List<LessonOrderUpdateRequest> requests) {
        log.info("Reordering lessons for course ID: {}", courseId);

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        for (LessonOrderUpdateRequest request : requests) {
            lessons.stream()
                    .filter(lesson -> lesson.getLessonId().equals(request.getLessonId()))
                    .findFirst()
                    .ifPresent(lesson -> lesson.setOrderIndex(request.getOrderIndex()));
        }

        List<Lesson> savedLessons = lessonRepository.saveAll(lessons);

        log.info("Reordered {} lessons for course ID: {}", savedLessons.size(), courseId);

        return savedLessons.stream()
                .sorted(Comparator.comparing(Lesson::getOrderIndex))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ResourceResponse addResource(UUID lessonId, ResourceRequest request) {
        log.info("Adding resource to lesson ID: {}", lessonId);

        if (!lessonRepository.existsById(lessonId)) {
            log.error("Lesson not found for adding resource. Lesson ID: {}", lessonId);
            throw new RuntimeException("Lesson not found");
        }

        Resource resource = Resource.builder()
                .lessonId(lessonId)
                .name(request.getName())
                .fileType(request.getFileType())
                .fileUrl(request.getFileUrl())
                .build();

        Resource saved = resourceRepository.save(resource);

        log.info("Resource added successfully with ID: {}", saved.getResourceId());

        return mapToResourceResponse(saved);
    }

    @Override
    public void removeResource(UUID resourceId) {
        log.info("Removing resource with ID: {}", resourceId);

        if (!resourceRepository.existsById(resourceId)) {
            log.error("Resource not found with ID: {}", resourceId);
            throw new RuntimeException("Resource not found");
        }

        resourceRepository.deleteById(resourceId);

        log.info("Resource removed successfully with ID: {}", resourceId);
    }

    @Override
    public List<LessonResponse> getPreviewLessons(UUID courseId) {
        log.info("Fetching preview lessons for course ID: {}", courseId);

        List<LessonResponse> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .limit(3)
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} preview lessons for course ID: {}", lessons.size(), courseId);
        return lessons;
    }

    private LessonResponse mapToResponse(Lesson lesson) {
        return LessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .courseId(lesson.getCourseId())
                .orderIndex(lesson.getOrderIndex())
                .contentUrl(lesson.getContentUrl())
                .contentType(lesson.getContentType())
                .durationMinutes(lesson.getDurationMinutes())
                .isPreview(lesson.getIsPreview())
                .build();
    }

    private ResourceResponse mapToResourceResponse(Resource resource) {
        return ResourceResponse.builder()
                .resourceId(resource.getResourceId())
                .lessonId(resource.getLessonId())
                .name(resource.getName())
                .fileType(resource.getFileType())
                .fileUrl(resource.getFileUrl())
                .build();
    }
}