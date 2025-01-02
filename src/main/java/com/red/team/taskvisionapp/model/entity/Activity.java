package com.red.team.taskvisionapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Relasi ke tabel Project

    @Column(nullable = false)
    private String entity; // Entity yang berubah (e.g., Task, Report)

    @Column(nullable = false)
    private String action; // Aksi yang dilakukan (e.g., Created, Updated)

    @Column(columnDefinition = "TEXT")
    private String details; // Detail aktivitas

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
