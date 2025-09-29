package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.EventRegisterRequest;
import com.example.S_PACE.dto.response.EventRegistrationResponse;
import com.example.S_PACE.enums.EventRegistrationStatus;

import java.util.List;
import java.util.UUID;

public interface EventRegistrationService {
    
    EventRegistrationResponse registerForEvent(EventRegisterRequest request, UUID userId);
    
    EventRegistrationResponse updateRegistrationStatus(UUID registrationId, EventRegistrationStatus status, String reviewNotes, UUID reviewerId);
    
    List<EventRegistrationResponse> getAllRegistrations();
    
    List<EventRegistrationResponse> getRegistrationsByUserId(UUID userId);
    
    List<EventRegistrationResponse> getRegistrationsByEventId(UUID eventId);
    
    EventRegistrationResponse getRegistrationById(UUID registrationId);
    
    List<EventRegistrationResponse> getRegistrationsByStatus(EventRegistrationStatus status);
    
    EventRegistrationResponse cancelRegistration(UUID registrationId, UUID userId);
}


