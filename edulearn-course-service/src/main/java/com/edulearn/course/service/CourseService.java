package com.edulearn.course.service;

import com.edulearn.course.dto.CourseRequest;
import com.edulearn.course.dto.CourseResponse;

import java.util.List;
import java.util.UUID;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request);

    List<CourseResponse> getAllCourses();

    CourseResponse getCourseById(UUID courseId);

    List<CourseResponse> getCoursesByCategory(String category);

    List<CourseResponse> getCoursesByInstructor(UUID instructorId);

    List<CourseResponse> searchCourses(String keyword);

    CourseResponse updateCourse(UUID courseId, CourseRequest request);

    void deleteCourse(UUID courseId);

    CourseResponse publishCourse(UUID courseId, Boolean published);

    CourseResponse submitCourseForApproval(UUID courseId);

    List<CourseResponse> getCoursesByStatus(String status);

    CourseResponse reviewCourse(UUID courseId, String action);
    
    List<CourseResponse> getPublishedCourses();
}