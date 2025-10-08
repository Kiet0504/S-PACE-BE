package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.EventRequest;
import com.example.S_PACE.dto.response.EventResponse;
import com.example.S_PACE.enums.EventStatus;
import com.example.S_PACE.mapper.EventMapper;
import com.example.S_PACE.pojo.Company;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.service.EventService;
import com.example.S_PACE.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest eventRequest, UUID companyId, UUID userId) {
        logger.info("Creating new event: {} for company: {} by user: {}", eventRequest.getEventName(), companyId, userId);

        if (eventRequest.getEventName() == null || eventRequest.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be null or empty");
        }

        if (companyId == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Check subscription limits
        if (eventRequest.getMaxParticipants() != null && eventRequest.getMaxParticipants() > 0) {
            int userLimit = subscriptionService.getRecruitmentLimitForUser(userId);
            if (eventRequest.getMaxParticipants() > userLimit) {
                String planName = getCurrentPlanName(userId);
                throw new IllegalArgumentException(
                    String.format("Maximum participants (%d) exceeds your %s plan limit of %d. Please upgrade your subscription to create events with more participants.",
                        eventRequest.getMaxParticipants(), planName, userLimit));
            }
        }

        try {
            // Get company reference
            Company company = entityManager.getReference(Company.class, companyId);

            // Convert EventRequest to Event entity
            Event event = eventMapper.toEvent(eventRequest);
            event.setCompany(company);
            event.setCreatedBy(userId);

            // Set default status if not provided
            if (event.getStatus() == null) {
                event.setStatus(EventStatus.DRAFT);
            }

            // Save event
            Event savedEvent = eventRepository.save(event);
            logger.info("Event created successfully with ID: {}", savedEvent.getEventId());

            return eventMapper.toEventResponse(savedEvent);

        } catch (Exception ex) {
            logger.error("Error creating event: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to create event: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID eventId) {
        logger.info("Fetching event by ID: {}", eventId);

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        return eventMapper.toEventResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        logger.info("Fetching all events");

        List<Event> events = eventRepository.findAll();
        return eventMapper.toEventResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByCompany(UUID companyId) {
        logger.info("Fetching events for company: {}", companyId);

        if (companyId == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }

        List<Event> events = eventRepository.findByCompanyCompanyId(companyId);
        return eventMapper.toEventResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByStatus(EventStatus status) {
        logger.info("Fetching events by status: {}", status);

        if (status == null) {
            throw new IllegalArgumentException("Event status cannot be null");
        }

        List<Event> events = eventRepository.findByStatus(status);
        return eventMapper.toEventResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByCompanyAndStatus(UUID companyId, EventStatus status) {
        logger.info("Fetching events for company: {} with status: {}", companyId, status);

        if (companyId == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Event status cannot be null");
        }

        List<Event> events = eventRepository.findByCompanyIdAndStatus(companyId, status);
        return eventMapper.toEventResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> searchEventsByName(String eventName) {
        logger.info("Searching events by name: {}", eventName);

        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be null or empty");
        }

        List<Event> events = eventRepository.findByTitleContainingIgnoreCase(eventName.trim());
        return eventMapper.toEventResponseList(events);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(UUID eventId, EventRequest eventRequest) {
        logger.info("Updating event with ID: {}", eventId);

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        if (eventRequest.getEventName() == null || eventRequest.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be null or empty");
        }

        try {
            Event existingEvent = eventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

            // Update event fields using mapper
            eventMapper.updateEventFromRequest(eventRequest, existingEvent);

            // Save updated event
            Event updatedEvent = eventRepository.save(existingEvent);
            logger.info("Event updated successfully with ID: {}", updatedEvent.getEventId());

            return eventMapper.toEventResponse(updatedEvent);

        } catch (Exception ex) {
            logger.error("Error updating event: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to update event: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteEvent(UUID eventId) {
        logger.info("Deleting event with ID: {}", eventId);

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("Event not found with ID: " + eventId);
        }

        try {
            eventRepository.deleteById(eventId);
            logger.info("Event deleted successfully with ID: {}", eventId);
        } catch (Exception ex) {
            logger.error("Error deleting event: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete event: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID eventId) {
        if (eventId == null) {
            return false;
        }
        return eventRepository.existsById(eventId);
    }

    private String getCurrentPlanName(UUID userId) {
        try {
            return subscriptionService.getUserCurrentPlan(userId).getPlanName();
        } catch (Exception e) {
            return "Free";
        }
    }
}