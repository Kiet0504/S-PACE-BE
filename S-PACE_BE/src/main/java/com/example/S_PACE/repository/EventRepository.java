package com.example.S_PACE.repository;

import com.example.S_PACE.enums.EventStatus;
import com.example.S_PACE.pojo.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    
    List<Event> findByCompanyCompanyId(UUID companyId);
    
    List<Event> findByStatus(EventStatus status);
    
    @Query("SELECT e FROM Event e WHERE e.company.companyId = :companyId AND e.status = :status")
    List<Event> findByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") EventStatus status);
} 