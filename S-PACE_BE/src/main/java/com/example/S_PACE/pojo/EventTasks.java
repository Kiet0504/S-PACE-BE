package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.EventTasksStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID eventTasksId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamId", referencedColumnName = "teamId", nullable = false)
    Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignedTo", referencedColumnName = "userId")
    User assignedTo;

    @Column(nullable = false, length = 255)
    String title;

    @Lob
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
