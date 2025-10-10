package com.example.S_PACE.repository;

import com.example.S_PACE.enums.EmailType;
import com.example.S_PACE.pojo.EmailNotifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface
EmailNotificationsRepository extends JpaRepository<EmailNotifications, UUID> {

    List<EmailNotifications> findByUserUserId(UUID userId);

    List<EmailNotifications> findByEventEventId(UUID eventId);

    List<EmailNotifications> findByEmailType(EmailType emailType);

    List<EmailNotifications> findByEventRegistrationEventRegistrationId(UUID eventRegistrationId);
}
