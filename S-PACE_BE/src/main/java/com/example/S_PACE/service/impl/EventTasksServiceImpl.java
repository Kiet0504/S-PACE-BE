package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.EventTasksRequest;
import com.example.S_PACE.dto.response.EventTasksResponse;
import com.example.S_PACE.enums.EventTasksStatus;
import com.example.S_PACE.mapper.EventTasksMapper;
import com.example.S_PACE.pojo.EventTasks;
import com.example.S_PACE.pojo.Team;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.EventTasksRepository;
import com.example.S_PACE.repository.TeamRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.service.EventTasksService;
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
public class EventTasksServiceImpl implements EventTasksService {

    private static final Logger logger = LoggerFactory.getLogger(EventTasksServiceImpl.class);

    @Autowired
    private EventTasksRepository eventTasksRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventTasksMapper eventTasksMapper;

    @Override
    @Transactional
    public EventTasksResponse create(EventTasksRequest request) {
        logger.info("Creating event task with title: {}", request.getTitle());

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title is required");
        }

        if (request.getTeamId() == null) {
            throw new IllegalArgumentException("Team ID is required");
        }

        if (request.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline is required");
        }

        Team team = teamRepository.findById(request.getTeamId())
            .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + request.getTeamId()));

        EventTasks eventTask = eventTasksMapper.toEntity(request);
        eventTask.setTeam(team);

        if (request.getAssignedToUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getAssignedToUserId()));
            eventTask.setAssignedTo(assignedUser);
        }

        if (request.getStatus() == null) {
            eventTask.setStatus(EventTasksStatus.TODO);
        }

        EventTasks savedTask = eventTasksRepository.save(eventTask);
        logger.info("Event task created successfully with ID: {}", savedTask.getEventTasksId());

        return eventTasksMapper.toEventTasksResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public EventTasksResponse getById(UUID id) {
        logger.info("Fetching event task with ID: {}", id);

        EventTasks eventTask = eventTasksRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event task not found with ID: " + id));

        return eventTasksMapper.toEventTasksResponse(eventTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTasksResponse> getAll() {
        logger.info("Fetching all event tasks");

        return eventTasksRepository.findAll().stream()
            .map(eventTasksMapper::toEventTasksResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTasksResponse> getByTeamId(UUID teamId) {
        logger.info("Fetching event tasks for team ID: {}", teamId);

        return eventTasksRepository.findByTeamId(teamId).stream()
            .map(eventTasksMapper::toEventTasksResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTasksResponse> getByAssignedUser(UUID userId) {
        logger.info("Fetching event tasks assigned to user ID: {}", userId);

        return eventTasksRepository.findByAssignedToUserId(userId).stream()
            .map(eventTasksMapper::toEventTasksResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTasksResponse> getByStatus(EventTasksStatus status) {
        logger.info("Fetching event tasks with status: {}", status);

        return eventTasksRepository.findByStatus(status).stream()
            .map(eventTasksMapper::toEventTasksResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTasksResponse> getByTeamIdAndStatus(UUID teamId, EventTasksStatus status) {
        logger.info("Fetching event tasks for team ID: {} with status: {}", teamId, status);

        return eventTasksRepository.findByTeamIdAndStatus(teamId, status).stream()
            .map(eventTasksMapper::toEventTasksResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTasksResponse> getByDeadlineBetween(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching event tasks with deadline between {} and {}", startDate, endDate);

        return eventTasksRepository.findByDeadlineBetween(startDate, endDate).stream()
            .map(eventTasksMapper::toEventTasksResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTasksResponse> getByEventId(UUID eventId) {
        logger.info("Fetching event tasks for event ID: {}", eventId);

        return eventTasksRepository.findByEventId(eventId).stream()
            .map(eventTasksMapper::toEventTasksResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventTasksResponse update(UUID id, EventTasksRequest request) {
        logger.info("Updating event task with ID: {}", id);

        EventTasks existingTask = eventTasksRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event task not found with ID: " + id));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            existingTask.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            existingTask.setDescription(request.getDescription());
        }

        if (request.getDeadline() != null) {
            existingTask.setDeadline(request.getDeadline());
        }

        if (request.getStatus() != null) {
            existingTask.setStatus(request.getStatus());
        }

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + request.getTeamId()));
            existingTask.setTeam(team);
        }

        if (request.getAssignedToUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getAssignedToUserId()));
            existingTask.setAssignedTo(assignedUser);
        }

        EventTasks updatedTask = eventTasksRepository.save(existingTask);
        logger.info("Event task updated successfully with ID: {}", updatedTask.getEventTasksId());

        return eventTasksMapper.toEventTasksResponse(updatedTask);
    }

    @Override
    @Transactional
    public EventTasksResponse updateStatus(UUID id, EventTasksStatus status) {
        logger.info("Updating status of event task with ID: {} to {}", id, status);

        EventTasks existingTask = eventTasksRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event task not found with ID: " + id));

        existingTask.setStatus(status);
        EventTasks updatedTask = eventTasksRepository.save(existingTask);

        logger.info("Event task status updated successfully");
        return eventTasksMapper.toEventTasksResponse(updatedTask);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        logger.info("Deleting event task with ID: {}", id);

        EventTasks existingTask = eventTasksRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event task not found with ID: " + id));

        eventTasksRepository.delete(existingTask);
        logger.info("Event task deleted successfully");
    }
}