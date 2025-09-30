package com.example.S_PACE.repository;

import com.example.S_PACE.enums.EventRegistrationStatus;
import com.example.S_PACE.pojo.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {

    List<EventRegistration> findByUserUserId(UUID userId);
    
    List<EventRegistration> findByEventEventId(UUID eventId);
    
    List<EventRegistration> findByStatus(EventRegistrationStatus status);
    
    @Query("SELECT er FROM EventRegistration er WHERE er.event.eventId = :eventId AND er.status = :status")
    List<EventRegistration> findByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") EventRegistrationStatus status);
    
    @Query("SELECT er FROM EventRegistration er WHERE er.user.userId = :userId AND er.event.eventId = :eventId")
    Optional<EventRegistration> findByUserIdAndEventId(@Param("userId") UUID userId, @Param("eventId") UUID eventId);
    
    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.event.eventId = :eventId AND er.status = 'APPROVED'")
    Long countApprovedRegistrationsByEventId(@Param("eventId") UUID eventId);
}

