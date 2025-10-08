package com.example.S_PACE.repository;

import com.example.S_PACE.enums.AttendanceStatus;
import com.example.S_PACE.pojo.AttendanceLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceLogsRepository extends JpaRepository<AttendanceLogs, UUID> {

    @Query("SELECT a FROM AttendanceLogs a LEFT JOIN FETCH a.event LEFT JOIN FETCH a.user")
    List<AttendanceLogs> findAllWithEventAndUser();

    @Query("SELECT a FROM AttendanceLogs a LEFT JOIN FETCH a.event LEFT JOIN FETCH a.user WHERE a.event.eventId = :eventId")
    List<AttendanceLogs> findByEventEventId(@Param("eventId") UUID eventId);

    @Query("SELECT a FROM AttendanceLogs a LEFT JOIN FETCH a.event LEFT JOIN FETCH a.user WHERE a.user.userId = :userId")
    List<AttendanceLogs> findByUserUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM AttendanceLogs a LEFT JOIN FETCH a.event LEFT JOIN FETCH a.user WHERE a.status = :status")
    List<AttendanceLogs> findByStatus(@Param("status") AttendanceStatus status);

    @Query("SELECT a FROM AttendanceLogs a LEFT JOIN FETCH a.event LEFT JOIN FETCH a.user WHERE a.event.eventId = :eventId AND a.status = :status")
    List<AttendanceLogs> findByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") AttendanceStatus status);

    @Query("SELECT a FROM AttendanceLogs a LEFT JOIN FETCH a.event LEFT JOIN FETCH a.user WHERE a.event.eventId = :eventId AND a.user.userId = :userId")
    List<AttendanceLogs> findByEventIdAndUserId(@Param("eventId") UUID eventId, @Param("userId") UUID userId);
}
