package com.edulearn.progress.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CertificateResponse {

    private UUID certificateId;
    private UUID studentId;
    private UUID courseId;
    private LocalDateTime issuedAt;
    private String certificateUrl;
}