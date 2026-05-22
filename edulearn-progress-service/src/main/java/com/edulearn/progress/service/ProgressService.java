package com.edulearn.progress.service;

import com.edulearn.progress.dto.CertificateResponse;
import com.edulearn.progress.dto.ProgressResponse;

import java.util.UUID;

public interface ProgressService {

    void completeLesson(UUID studentId, UUID courseId, UUID lessonId);

    ProgressResponse getProgress(UUID studentId, UUID courseId);

    CertificateResponse getCertificate(UUID studentId, UUID courseId);
}