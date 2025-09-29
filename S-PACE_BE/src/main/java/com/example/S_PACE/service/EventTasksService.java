package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.EventTasksRequest;
import com.example.S_PACE.dto.response.EventTasksResponse;
import com.example.S_PACE.enums.EventTasksStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventTasksService {
    EventTasksResponse create(EventTasksRequest request);
    EventTasksResponse getById(UUID id);
    List<EventTasksResponse> getAll();
    List<EventTasksResponse> getByTeamId(UUID teamId);
    List<EventTasksResponse> getByAssignedUser(UUID userId);
    List<EventTasksResponse> getByStatus(EventTasksStatus status);
    List<EventTasksResponse> getByTeamIdAndStatus(UUID teamId, EventTasksStatus status);
    List<EventTasksResponse> getByDeadlineBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<EventTasksResponse> getByEventId(UUID eventId);
    EventTasksResponse update(UUID id, EventTasksRequest request);
    EventTasksResponse updateStatus(UUID id, EventTasksStatus status);
    void delete(UUID id);
}