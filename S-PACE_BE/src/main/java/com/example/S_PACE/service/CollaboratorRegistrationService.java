package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.CollaboratorRegistrationRequest;
import com.example.S_PACE.dto.response.CollaboratorRegistrationResponse;
import com.example.S_PACE.enums.EventRegistrationStatus;

import java.util.List;
import java.util.UUID;

public interface CollaboratorRegistrationService {

    /**
     * Register a collaborator for an event
     */
    CollaboratorRegistrationResponse registerCollaborator(CollaboratorRegistrationRequest request);

    /**
     * Get all collaborator registrations
     */
    List<CollaboratorRegistrationResponse> getAllRegistrations();

    /**
     * Get collaborator registrations by event ID
     */
    List<CollaboratorRegistrationResponse> getRegistrationsByEventId(UUID eventId);

    /**
     * Get collaborator registrations by user ID
     */
    List<CollaboratorRegistrationResponse> getRegistrationsByUserId(UUID userId);

    /**
     * Get collaborator registration by ID
     */
    CollaboratorRegistrationResponse getRegistrationById(UUID registrationId);

    /**
     * Update registration status
     */
    CollaboratorRegistrationResponse updateRegistrationStatus(UUID registrationId, 
                                                             EventRegistrationStatus status, 
                                                             String reviewNotes, 
                                                             UUID reviewedBy);

    /**
     * Cancel registration
     */
    CollaboratorRegistrationResponse cancelRegistration(UUID registrationId);

    /**
     * Check if user is already registered for an event
     */
    boolean isUserRegisteredForEvent(UUID eventId, UUID userId);

    /**
     * Get registration count by event ID
     */
    long getRegistrationCountByEventId(UUID eventId);

    /**
     * Get registration count by event ID and status
     */
    long getRegistrationCountByEventIdAndStatus(UUID eventId, EventRegistrationStatus status);
} 