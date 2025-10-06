package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.EventRegisterRequest;
import com.example.S_PACE.dto.response.EventRegistrationResponse;
import com.example.S_PACE.enums.EventRegistrationStatus;
import com.example.S_PACE.exception.AuthenticationException;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.EventRegistration;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.EventRegistrationRepository;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.service.EventRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(EventRegistrationServiceImpl.class);

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public EventRegistrationResponse registerForEvent(EventRegisterRequest request, UUID userId) {
        logger.info("Processing event registration for user: {} and event: {}", userId, request.getEventId());

        // Validate user exists and is a collaborator
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"COLLABORATOR".equals(user.getRole().getRoleName())) {
            throw new AuthenticationException("Only collaborators can register for events");
        }

        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if user already registered for this event
        eventRegistrationRepository.findByUserIdAndEventId(userId, request.getEventId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User already registered for this event");
                });

        // Create new registration
        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .fullName(request.getFullName())
                .gender(request.getGender())
                .profession(request.getProfession())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .reasonForParticipation(request.getReasonForParticipation())
                .filePath(request.getFilePath())
                .birthYear(request.getBirthYear())
                .status(EventRegistrationStatus.PENDING)
                .build();

        registration = eventRegistrationRepository.save(registration);
        logger.info("Event registration created successfully with ID: {}", registration.getEventRegistrationId());

        return mapToResponse(registration);
    }

    @Override
    public EventRegistrationResponse updateRegistrationStatus(UUID registrationId, EventRegistrationStatus status, 
                                                            String reviewNotes, UUID reviewerId) {
        logger.info("Updating registration status for ID: {} to status: {}", registrationId, status);

        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

        registration.setStatus(status);
        registration.setReviewNotes(reviewNotes);
        registration.setReviewedBy(reviewer);
        registration.setReviewedAt(LocalDateTime.now());

        registration = eventRegistrationRepository.save(registration);
        logger.info("Registration status updated successfully");

        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getAllRegistrations() {
        logger.info("Fetching all event registrations");
        return eventRegistrationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrationsByUserId(UUID userId) {
        logger.info("Fetching registrations for user: {}", userId);
        return eventRegistrationRepository.findByUserUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrationsByEventId(UUID eventId) {
        logger.info("Fetching registrations for event: {}", eventId);
        return eventRegistrationRepository.findByEventEventId(eventId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventRegistrationResponse getRegistrationById(UUID registrationId) {
        logger.info("Fetching registration by ID: {}", registrationId);
        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        
        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrationsByStatus(EventRegistrationStatus status) {
        logger.info("Fetching registrations with status: {}", status);
        return eventRegistrationRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventRegistrationResponse cancelRegistration(UUID registrationId, UUID userId) {
        logger.info("Cancelling registration: {} for user: {}", registrationId, userId);

        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Verify the user owns this registration
        if (!registration.getUser().getUserId().equals(userId)) {
            throw new AuthenticationException("You can only cancel your own registrations");
        }

        // Only allow cancellation of pending or approved registrations
        if (registration.getStatus() != EventRegistrationStatus.PENDING && 
            registration.getStatus() != EventRegistrationStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot cancel registration with status: " + registration.getStatus());
        }

        registration.setStatus(EventRegistrationStatus.CANCELLED);
        registration = eventRegistrationRepository.save(registration);

        logger.info("Registration cancelled successfully");
        return mapToResponse(registration);
    }

    private EventRegistrationResponse mapToResponse(EventRegistration registration) {
        return EventRegistrationResponse.builder()
                .eventRegistrationId(registration.getEventRegistrationId())
                .eventId(registration.getEvent().getEventId())
                .eventTitle(registration.getEvent().getTitle())
                .userId(registration.getUser().getUserId())
                .userEmail(registration.getUser().getEmail())
                .registrationDate(registration.getRegistrationDate())
                .reviewedBy(registration.getReviewedBy() != null ? registration.getReviewedBy().getUserId() : null)
                .reviewedByName(registration.getReviewedBy() != null ? registration.getReviewedBy().getFullName() : null)
                .reviewedAt(registration.getReviewedAt())
                .reviewNotes(registration.getReviewNotes())
                .status(registration.getStatus())
                .updatedAt(registration.getUpdatedAt())
                .fullName(registration.getFullName())
                .gender(registration.getGender())
                .profession(registration.getProfession())
                .phoneNumber(registration.getPhoneNumber())
                .address(registration.getAddress())
                .reasonForParticipation(registration.getReasonForParticipation())
                .filePath(registration.getFilePath())
                .birthYear(registration.getBirthYear())
                .build();
    }
}
