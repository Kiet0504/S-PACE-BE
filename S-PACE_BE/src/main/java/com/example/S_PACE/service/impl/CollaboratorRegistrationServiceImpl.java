package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.CollaboratorRegistrationRequest;
import com.example.S_PACE.dto.response.CollaboratorRegistrationResponse;
import com.example.S_PACE.enums.EventRegistrationStatus;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.EventRegistration;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.CollaboratorRegistrationRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.service.CollaboratorRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CollaboratorRegistrationServiceImpl implements CollaboratorRegistrationService {

    private final CollaboratorRegistrationRepository collaboratorRegistrationRepository;
    private final UserRepository userRepository;

    @Override
    public CollaboratorRegistrationResponse registerCollaborator(CollaboratorRegistrationRequest request) {
        log.info("Registering collaborator for event: {}", request.getEventId());

        // Validate that the user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user is already registered for this event
        if (collaboratorRegistrationRepository.existsByEventEventIdAndUserUserId(request.getEventId(), user.getUserId())) {
            throw new IllegalArgumentException("User is already registered for this event");
        }

        // Create new registration
        Event event = new Event();
        event.setEventId(request.getEventId());
        
        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .registrationDate(LocalDateTime.now())
                .status(EventRegistrationStatus.PENDING)
                .fullName(request.getFullName())
                .gender(request.getGender())
                .profession(request.getProfession())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .reasonForParticipation(request.getReasonForParticipation())
                .filePath(request.getFilePath())
                .build();

        EventRegistration savedRegistration = collaboratorRegistrationRepository.save(registration);
        log.info("Collaborator registered successfully with ID: {}", savedRegistration.getEventRegistrationId());

        return mapToResponse(savedRegistration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollaboratorRegistrationResponse> getAllRegistrations() {
        log.info("Fetching all collaborator registrations");
        List<EventRegistration> registrations = collaboratorRegistrationRepository.findAllWithDetails();
        return registrations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollaboratorRegistrationResponse> getRegistrationsByEventId(UUID eventId) {
        log.info("Fetching registrations for event: {}", eventId);
        List<EventRegistration> registrations = collaboratorRegistrationRepository.findByEventEventId(eventId);
        return registrations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollaboratorRegistrationResponse> getRegistrationsByUserId(UUID userId) {
        log.info("Fetching registrations for user: {}", userId);
        List<EventRegistration> registrations = collaboratorRegistrationRepository.findByUserUserId(userId);
        return registrations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CollaboratorRegistrationResponse getRegistrationById(UUID registrationId) {
        log.info("Fetching registration by ID: {}", registrationId);
        EventRegistration registration = collaboratorRegistrationRepository.findByIdWithDetails(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        return mapToResponse(registration);
    }

    @Override
    public CollaboratorRegistrationResponse updateRegistrationStatus(UUID registrationId, 
                                                                   EventRegistrationStatus status, 
                                                                   String reviewNotes, 
                                                                   UUID reviewedBy) {
        log.info("Updating registration status for ID: {} to {}", registrationId, status);
        
        EventRegistration registration = collaboratorRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        User reviewer = userRepository.findById(reviewedBy)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

        registration.setStatus(status);
        registration.setReviewNotes(reviewNotes);
        registration.setReviewedBy(reviewer);
        registration.setReviewedAt(LocalDateTime.now());

        EventRegistration updatedRegistration = collaboratorRegistrationRepository.save(registration);
        log.info("Registration status updated successfully");

        return mapToResponse(updatedRegistration);
    }

    @Override
    public CollaboratorRegistrationResponse cancelRegistration(UUID registrationId) {
        log.info("Cancelling registration with ID: {}", registrationId);
        
        EventRegistration registration = collaboratorRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        registration.setStatus(EventRegistrationStatus.CANCELLED);

        EventRegistration updatedRegistration = collaboratorRegistrationRepository.save(registration);
        log.info("Registration cancelled successfully");

        return mapToResponse(updatedRegistration);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserRegisteredForEvent(UUID eventId, UUID userId) {
        return collaboratorRegistrationRepository.existsByEventEventIdAndUserUserId(eventId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getRegistrationCountByEventId(UUID eventId) {
        return collaboratorRegistrationRepository.countByEventEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getRegistrationCountByEventIdAndStatus(UUID eventId, EventRegistrationStatus status) {
        return collaboratorRegistrationRepository.countByEventEventIdAndStatus(eventId, status);
    }

    private CollaboratorRegistrationResponse mapToResponse(EventRegistration registration) {
        return CollaboratorRegistrationResponse.builder()
                .eventRegistrationId(registration.getEventRegistrationId())
                .eventId(registration.getEvent().getEventId())
                .eventTitle(registration.getEvent().getTitle())
                .userId(registration.getUser().getUserId())
                .userEmail(registration.getUser().getEmail())
                .registrationDate(registration.getRegistrationDate())
                .status(registration.getStatus())
                .fullName(registration.getFullName())
                .gender(registration.getGender())
                .profession(registration.getProfession())
                .phoneNumber(registration.getPhoneNumber())
                .address(registration.getAddress())
                .reasonForParticipation(registration.getReasonForParticipation())
                .filePath(registration.getFilePath())
                .updatedAt(registration.getUpdatedAt())
                .build();
    }
}