package com.example.S_PACE.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "taskReports")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TaskReports {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "task_reports_id")
    UUID taskReportsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_tasks_id", referencedColumnName = "event_tasks_id", nullable = false)
    EventTasks eventTasks;

    @Column(nullable = false, precision = 5, scale = 2)
    BigDecimal progress;

    @Column(columnDefinition = "TEXT")
    String note;

    @Column(nullable = false, updatable = false)
    LocalDateTime reportedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User reportedBy;
}
