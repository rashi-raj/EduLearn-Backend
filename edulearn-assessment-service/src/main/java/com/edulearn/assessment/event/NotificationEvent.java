package com.edulearn.assessment.event;

import lombok.*;
import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationEvent implements Serializable {
    private String eventType;
    private String userId;
    private String title;
    private String message;
}
