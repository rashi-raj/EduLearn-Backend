package com.edulearn.lesson.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID resourceId;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Column(nullable = false, length = 50)
    private String fileType;

    @Column
    private Integer sizeKb;
}