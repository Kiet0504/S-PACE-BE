package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.EventTasksStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "eventTasks")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventTasks {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_tasks_id")
    UUID eventTasksId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", referencedColumnName = "team_id", nullable = false)
    Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignedTo", referencedColumnName = "user_id")
    User assignedTo;

    @Column(nullable = false, length = 255)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EventTasksStatus status = EventTasksStatus.TODO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
