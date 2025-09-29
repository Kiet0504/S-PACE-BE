package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.EventRegistration;
import com.example.S_PACE.enums.EventRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollaboratorRegistrationRepository extends JpaRepository<EventRegistration, UUID> {

    // Find registrations by event ID
    List<EventRegistration> findByEventEventId(UUID eventId);

    // Find registrations by user ID
    List<EventRegistration> findByUserUserId(UUID userId);

    // Find registration by event ID and user ID
    Optional<EventRegistration> findByEventEventIdAndUserUserId(UUID eventId, UUID userId);

    // Find registrations by status
    List<EventRegistration> findByStatus(EventRegistrationStatus status);

    // Find registrations by event ID and status
    List<EventRegistration> findByEventEventIdAndStatus(UUID eventId, EventRegistrationStatus status);

    // Check if user already registered for an event
    boolean existsByEventEventIdAndUserUserId(UUID eventId, UUID userId);

    // Count registrations by event ID
    long countByEventEventId(UUID eventId);

    // Count registrations by event ID and status
    long countByEventEventIdAndStatus(UUID eventId, EventRegistrationStatus status);

    // Find registrations with event and user details
    @Query("SELECT er FROM EventRegistration er " +
           "JOIN FETCH er.event e " +
           "JOIN FETCH er.user u " +
           "WHERE er.eventRegistrationId = :registrationId")
    Optional<EventRegistration> findByIdWithDetails(@Param("registrationId") UUID registrationId);

    // Find all registrations with event and user details
    @Query("SELECT er FROM EventRegistration er " +
           "JOIN FETCH er.event e " +
           "JOIN FETCH er.user u " +
           "ORDER BY er.registrationDate DESC")
    List<EventRegistration> findAllWithDetails();
} 