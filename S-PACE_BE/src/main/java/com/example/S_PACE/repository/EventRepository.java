package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    // Find events by company ID
    List<Event> findByCompanyCompanyId(UUID companyId);

    // Find events by status
    List<Event> findByStatus(EventStatus status);

    // Find events by company ID and status
    List<Event> findByCompanyCompanyIdAndStatus(UUID companyId, EventStatus status);

    // Find events by date range
    List<Event> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Find events by start date
    List<Event> findByStartDate(LocalDate startDate);

    // Find events by end date
    List<Event> findByEndDate(LocalDate endDate);

    // Find upcoming events (start date >= today)
    @Query("SELECT e FROM Event e WHERE e.startDate >= :today ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(@Param("today") LocalDate today);

    // Find past events (end date < today)
    @Query("SELECT e FROM Event e WHERE e.endDate < :today ORDER BY e.endDate DESC")
    List<Event> findPastEvents(@Param("today") LocalDate today);

    // Find events with company details
    @Query("SELECT e FROM Event e JOIN FETCH e.company c WHERE e.eventId = :eventId")
    Optional<Event> findByIdWithCompany(@Param("eventId") UUID eventId);

    // Find all events with company details
    @Query("SELECT e FROM Event e JOIN FETCH e.company c ORDER BY e.startDate DESC")
    List<Event> findAllWithCompany();
} 