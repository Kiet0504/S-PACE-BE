package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.EventRequest;
import com.example.S_PACE.dto.response.EventResponse;
import com.example.S_PACE.enums.EventStatus;
import com.example.S_PACE.mapper.EventMapper;
import com.example.S_PACE.pojo.Company;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.CompanyRepository;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.service.EventService;
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
    private CompanyRepository companyRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest eventRequest, UUID companyId, UUID createdBy) {
        logger.info("Creating new event: {} for company: {}", eventRequest.getEventName(), companyId);

        if (eventRequest.getEventName() == null || eventRequest.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be null or empty");
        }

        if (companyId == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }

        try {
            // Validate company status - only ACTIVE companies can create events
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
            
            if (!company.getStatus().canCreateEvents()) {
                throw new IllegalArgumentException("Only companies with ACTIVE status can create events. Current company status: " + company.getStatus());
            }
            
            logger.info("Company status validation passed for company: {} with status: {}", companyId, company.getStatus());
            
            // Get user reference for createdBy
            User createdByUser = entityManager.getReference(User.class, createdBy);

            // Convert EventRequest to Event entity
            Event event = eventMapper.toEvent(eventRequest);
            event.setCompany(company);
            event.setCreatedBy(createdByUser);

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
    public List<EventResponse> getEventsByStatus(EventStatus status, int page, int limit, String sortBy, String order) {
        logger.info("Fetching events by status: {} with pagination - page: {}, limit: {}, sortBy: {}, order: {}", 
                   status, page, limit, sortBy, order);

        if (status == null) {
            throw new IllegalArgumentException("Event status cannot be null");
        }

        // For now, we'll use the existing repository method and implement pagination in memory
        // In a production environment, you might want to use Spring Data's Pageable for better performance
        List<Event> allEvents = eventRepository.findByStatus(status);
        
        // Apply sorting
        allEvents.sort((e1, e2) -> {
            int comparison = 0;
            switch (sortBy.toLowerCase()) {
                case "startdate":
                    comparison = e1.getStartDate().compareTo(e2.getStartDate());
                    break;
                case "enddate":
                    comparison = e1.getEndDate().compareTo(e2.getEndDate());
                    break;
                case "createdat":
                default:
                    comparison = e1.getCreatedAt().compareTo(e2.getCreatedAt());
                    break;
            }
            return "desc".equalsIgnoreCase(order) ? -comparison : comparison;
        });
        
        // Apply pagination
        int startIndex = (page - 1) * limit;
        int endIndex = Math.min(startIndex + limit, allEvents.size());
        
        if (startIndex >= allEvents.size()) {
            return List.of(); // Return empty list if page is out of range
        }
        
        List<Event> paginatedEvents = allEvents.subList(startIndex, endIndex);
        return eventMapper.toEventResponseList(paginatedEvents);
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

    @Override
    @Transactional
    public EventResponse updateEventStatus(UUID eventId, EventStatus newStatus, UUID userId) {
        logger.info("Updating event status for event: {} to status: {} by user: {}", eventId, newStatus, userId);

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Event status cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            // Get existing event
            Event existingEvent = eventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

            // Flexible mode: Allow any status transition without validation
            logger.info("Flexible mode: Allowing status transition from {} to {} for event: {} by user: {}", 
                       existingEvent.getStatus(), newStatus, eventId, userId);

            // Store old status for logging
            EventStatus oldStatus = existingEvent.getStatus();

            // Update status
            existingEvent.setStatus(newStatus);

            // Save updated event
            Event updatedEvent = eventRepository.save(existingEvent);
            logger.info("Event status updated successfully from {} to {} for event: {}", oldStatus, newStatus, eventId);

            return eventMapper.toEventResponse(updatedEvent);

        } catch (Exception ex) {
            logger.error("Error updating event status: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to update event status: " + ex.getMessage());
        }
    }

    /**
     * Validates if a status transition is allowed according to the business rules
     * 
     * @param currentStatus Current event status
     * @param newStatus New status to transition to
     * @return true if transition is valid, false otherwise
     */
    private boolean isValidStatusTransition(EventStatus currentStatus, EventStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        // Same status is always valid
        if (currentStatus == newStatus) {
            return true;
        }

        // Define valid transitions based on documentation
        switch (currentStatus) {
            case DRAFT:
                return newStatus == EventStatus.PUBLISHED || newStatus == EventStatus.CANCELLED;
            
            case PUBLISHED:
                return newStatus == EventStatus.REGISTRATION_OPEN || newStatus == EventStatus.CANCELLED;
            
            case REGISTRATION_OPEN:
                return newStatus == EventStatus.REGISTRATION_CLOSED || newStatus == EventStatus.CANCELLED;
            
            case REGISTRATION_CLOSED:
                return newStatus == EventStatus.ONGOING || newStatus == EventStatus.CANCELLED;
            
            case ONGOING:
                return newStatus == EventStatus.COMPLETED || newStatus == EventStatus.CANCELLED;
            
            case COMPLETED:
                // COMPLETED events cannot transition to any other status
                return false;
            
            case CANCELLED:
                // CANCELLED events cannot transition to any other status
                return false;
            
            case ACTIVE:
            case SUSPENDED:
                // These statuses are not part of the main workflow but allow transitions to any valid status
                return isValidMainStatus(newStatus);
            
            default:
                return false;
        }
    }

    /**
     * Checks if a status is part of the main event workflow
     */
    private boolean isValidMainStatus(EventStatus status) {
        return status == EventStatus.DRAFT || 
               status == EventStatus.PUBLISHED || 
               status == EventStatus.REGISTRATION_OPEN || 
               status == EventStatus.REGISTRATION_CLOSED || 
               status == EventStatus.ONGOING || 
               status == EventStatus.COMPLETED || 
               status == EventStatus.CANCELLED;
    }
}