package com.example.S_PACE.repository;

import com.example.S_PACE.enums.EventTasksStatus;
import com.example.S_PACE.pojo.EventTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventTasksRepository extends JpaRepository<EventTasks, UUID> {

    @Query("SELECT et FROM EventTasks et WHERE et.team.teamId = :teamId")
    List<EventTasks> findByTeamId(@Param("teamId") UUID teamId);

    @Query("SELECT et FROM EventTasks et WHERE et.assignedTo.userId = :userId")
    List<EventTasks> findByAssignedToUserId(@Param("userId") UUID userId);

    @Query("SELECT et FROM EventTasks et WHERE et.status = :status")
    List<EventTasks> findByStatus(@Param("status") EventTasksStatus status);

    @Query("SELECT et FROM EventTasks et WHERE et.team.teamId = :teamId AND et.status = :status")
    List<EventTasks> findByTeamIdAndStatus(@Param("teamId") UUID teamId, @Param("status") EventTasksStatus status);

    @Query("SELECT et FROM EventTasks et WHERE et.deadline BETWEEN :startDate AND :endDate")
    List<EventTasks> findByDeadlineBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT et FROM EventTasks et WHERE et.team.event.eventId = :eventId")
    List<EventTasks> findByEventId(@Param("eventId") UUID eventId);
}