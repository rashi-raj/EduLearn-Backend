package com.edulearn.auth.event;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent implements Serializable {
    private String eventType;
    private String userId;
    private String title;
    private String message;
}
