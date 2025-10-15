package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.EventRequest;
import com.example.S_PACE.dto.response.EventResponse;
import com.example.S_PACE.enums.EventStatus;

import java.util.List;
import java.util.UUID;

public interface EventService {
    EventResponse createEvent(EventRequest eventRequest, UUID companyId, UUID createdBy);
    EventResponse getEventById(UUID eventId);
    List<EventResponse> getAllEvents();
    List<EventResponse> getEventsByCompany(UUID companyId);
    List<EventResponse> getEventsByStatus(EventStatus status);
    List<EventResponse> getEventsByStatus(EventStatus status, int page, int limit, String sortBy, String order);
    List<EventResponse> getEventsByCompanyAndStatus(UUID companyId, EventStatus status);
    List<EventResponse> searchEventsByName(String eventName);
    EventResponse updateEvent(UUID eventId, EventRequest eventRequest);
    EventResponse updateEventStatus(UUID eventId, EventStatus newStatus, UUID userId);
    void deleteEvent(UUID eventId);
    boolean existsById(UUID eventId);
}