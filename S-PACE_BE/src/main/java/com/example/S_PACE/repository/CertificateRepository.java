package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.Certificates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificates, UUID> {
    
    List<Certificates> findByEventEventId(UUID eventId);
    
    List<Certificates> findByUserUserId(UUID userId);
    
    Optional<Certificates> findByEventEventIdAndUserUserId(UUID eventId, UUID userId);
    
    Optional<Certificates> findByCertificateCode(String certificateCode);
    
    @Query("SELECT c FROM Certificates c WHERE c.event.eventId = :eventId AND c.user.userId = :userId")
    Optional<Certificates> findCertificateByEventAndUser(@Param("eventId") UUID eventId, @Param("userId") UUID userId);
    
    boolean existsByEventEventIdAndUserUserId(UUID eventId, UUID userId);
    
    boolean existsByCertificateCode(String certificateCode);
}
