package com.edulearn.course.service.impl;

import com.edulearn.course.dto.CourseRequest;
import com.edulearn.course.dto.CourseResponse;
import com.edulearn.course.entity.Course;
import com.edulearn.course.entity.CourseStatus;
import com.edulearn.course.event.NotificationEvent;
import com.edulearn.course.event.NotificationEventPublisher;
import com.edulearn.course.repository.CourseRepository;
import com.edulearn.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseRepository courseRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public CourseResponse createCourse(CourseRequest request) {
        log.info("Creating course with title: {} for instructor: {}", request.getTitle(), request.getInstructorId());

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .level(request.getLevel())
                .price(request.getPrice())
                .instructorId(request.getInstructorId())
                .thumbnailUrl(request.getThumbnailUrl())
                .totalDuration(request.getTotalDuration())
                .isPublished(false)
                .status(CourseStatus.DRAFT)
                .language(request.getLanguage())
                .validityInMonths(request.getValidityInMonths())
                .createdAt(LocalDateTime.now())
                .build();

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getCourseId());

        return mapToResponse(savedCourse);
    }

    @Override
    public List<CourseResponse> getAllCourses() {
        log.info("Fetching all courses");

        List<CourseResponse> courses = courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} courses", courses.size());
        return courses;
    }

    @Override
    public CourseResponse getCourseById(UUID courseId) {
        log.info("Fetching course by ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", courseId);
                    return new RuntimeException("Course not found");
                });

        log.info("Course fetched successfully with ID: {}", courseId);
        return mapToResponse(course);
    }

    @Override
    public List<CourseResponse> getCoursesByCategory(String category) {
        log.info("Fetching courses by category: {}", category);

        List<CourseResponse> courses = courseRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} courses for category: {}", courses.size(), category);
        return courses;
    }

    @Override
    public List<CourseResponse> getCoursesByInstructor(UUID instructorId) {
        log.info("Fetching courses by instructor ID: {}", instructorId);

        List<CourseResponse> courses = courseRepository.findByInstructorId(instructorId)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} courses for instructor ID: {}", courses.size(), instructorId);
        return courses;
    }

    @Override
    public List<CourseResponse> searchCourses(String keyword) {
        log.info("Searching courses with keyword: {}", keyword);

        List<CourseResponse> courses = courseRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Search completed. Found {} courses for keyword: {}", courses.size(), keyword);
        return courses;
    }

    @Override
    public CourseResponse updateCourse(UUID courseId, CourseRequest request) {
        log.info("Updating course with ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found for update with ID: {}", courseId);
                    return new RuntimeException("Course not found");
                });

        if (course.getStatus() == CourseStatus.PUBLISHED) {
            log.error("Attempted to update PUBLISHED course ID: {}", courseId);
            throw new RuntimeException("Published courses cannot be edited to protect student enrollments. Please unpublish the course first.");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setLevel(request.getLevel());
        course.setPrice(request.getPrice());
        course.setInstructorId(request.getInstructorId());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setTotalDuration(request.getTotalDuration());
        course.setLanguage(request.getLanguage());
        course.setValidityInMonths(request.getValidityInMonths());

        if (course.getStatus() == CourseStatus.REJECTED) {
            log.info("Rejected course with ID: {} moved back to DRAFT after update", courseId);
            course.setStatus(CourseStatus.DRAFT);
            course.setIsPublished(false);
        }

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully with ID: {}", updatedCourse.getCourseId());

        return mapToResponse(updatedCourse);
    }

    @Override
    public void deleteCourse(UUID courseId) {
        log.info("Deleting course with ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found for deletion with ID: {}", courseId);
                    return new RuntimeException("Course not found");
                });

        if (course.getStatus() == CourseStatus.PUBLISHED) {
            log.error("Attempted to delete PUBLISHED course ID: {}", courseId);
            throw new RuntimeException("Published courses cannot be deleted while they are active.");
        }

        courseRepository.deleteById(courseId);
        log.info("Course deleted successfully with ID: {}", courseId);
    }

    @Override
    public CourseResponse publishCourse(UUID courseId, Boolean published) {
        log.info("Updating publish status for course ID: {} to {}", courseId, published);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found for publish/unpublish with ID: {}", courseId);
                    return new RuntimeException("Course not found");
                });

        course.setIsPublished(published);

        if (Boolean.TRUE.equals(published)) {
            // To publish, the course MUST have been approved by Admin (status = PUBLISHED)
            if (course.getStatus() != CourseStatus.PUBLISHED) {
                log.error("Attempted to publish unapproved course ID: {}. Current status: {}", courseId, course.getStatus());
                throw new RuntimeException("Course must be approved by admin before it can be published.");
            }
            course.setIsPublished(true);
            log.info("Course marked as VISIBLE (isPublished=true) with ID: {}", courseId);
        } else {
            course.setIsPublished(false);
            log.info("Course marked as HIDDEN (isPublished=false) with ID: {}", courseId);
        }

        Course updatedCourse = courseRepository.save(course);
        return mapToResponse(updatedCourse);
    }

    @Override
    public CourseResponse submitCourseForApproval(UUID courseId) {
        log.info("Submitting course for approval with ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found for approval submission with ID: {}", courseId);
                    return new RuntimeException("Course not found");
                });

        if (Boolean.TRUE.equals(course.getIsPublished())) {
            log.error("Published course does not need approval request. Course ID: {}", courseId);
            throw new RuntimeException("Published course does not need approval request");
        }

        if (course.getStatus() == CourseStatus.PENDING_APPROVAL) {
            log.error("Course is already pending approval. Course ID: {}", courseId);
            throw new RuntimeException("Course is already pending approval");
        }

        course.setStatus(CourseStatus.PENDING_APPROVAL);
        course.setIsPublished(false);
        // Ensure instructorId is preserved explicitly
        course.setInstructorId(course.getInstructorId());

        Course updatedCourse = courseRepository.save(course);
        log.info("Course submitted for approval successfully with ID: {}", courseId);

        return mapToResponse(updatedCourse);
    }

    @Override
    public List<CourseResponse> getCoursesByStatus(String status) {
        log.info("Fetching courses by status: {}", status);

        CourseStatus courseStatus;
        try {
            courseStatus = CourseStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid course status received: {}", status);
            throw new RuntimeException("Invalid course status: " + status);
        }

        List<CourseResponse> courses = courseRepository.findByStatus(courseStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} courses for status: {}", courses.size(), status);
        return courses;
    }

    @Override
    public CourseResponse reviewCourse(UUID courseId, String action) {
        System.out.println(">>> BACKEND: Reviewing course " + courseId + " with action: " + action);
        log.info("Reviewing course with ID: {} and action: {}", courseId, action);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if ("APPROVE".equalsIgnoreCase(action)) {
            course.setStatus(CourseStatus.PUBLISHED);
            course.setIsPublished(true);
            System.out.println(">>> BACKEND: Course status updated to PUBLISHED");
        } else {
            course.setStatus(CourseStatus.REJECTED);
            course.setIsPublished(false);
            System.out.println(">>> BACKEND: Course status updated to REJECTED");
        }

        Course saved = courseRepository.saveAndFlush(course);
        System.out.println(">>> BACKEND: Course saved and flushed to DB");

        try {
            notificationEventPublisher.publish(NotificationEvent.builder()
                    .eventType("APPROVE".equalsIgnoreCase(action) ? "COURSE_APPROVED" : "COURSE_REJECTED")
                    .userId(saved.getInstructorId().toString())
                    .title("APPROVE".equalsIgnoreCase(action) ? "Course Approved!" : "Course Rejected")
                    .message("Your course '" + saved.getTitle() + "' has been " + action.toLowerCase() + "d.")
                    .build());
            System.out.println(">>> BACKEND: Notification sent to Kafka");
        } catch (Exception e) {
            System.err.println(">>> BACKEND ERROR: Kafka failed but proceeding: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Override
    public List<CourseResponse> getPublishedCourses() {
        log.info("Fetching all published courses");

        List<CourseResponse> courses = courseRepository.findByIsPublishedTrueAndStatus(CourseStatus.PUBLISHED)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} published courses", courses.size());
        return courses;
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .level(course.getLevel())
                .price(course.getPrice())
                .instructorId(course.getInstructorId())
                .thumbnailUrl(course.getThumbnailUrl())
                .totalDuration(course.getTotalDuration())
                .isPublished(course.getIsPublished())
                .status(course.getStatus().name())
                .language(course.getLanguage())
                .validityInMonths(course.getValidityInMonths())
                .createdAt(course.getCreatedAt())
                .build();
    }
}