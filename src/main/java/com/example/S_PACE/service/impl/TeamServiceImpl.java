package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.TeamRequest;
import com.example.S_PACE.dto.response.TeamResponse;
import com.example.S_PACE.mapper.TeamMapper;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.Team;
import com.example.S_PACE.repository.TeamRepository;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.service.TeamService;
import com.example.S_PACE.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeamServiceImpl implements TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    @Transactional
    public TeamResponse create(TeamRequest request) {
        logger.info("Creating team with name: {}", request.getTeamName());

        if (request.getTeamName() == null || request.getTeamName().trim().isEmpty()) {
            throw new IllegalArgumentException("Team name is required");
        }

        if (request.getEventId() == null) {
            throw new IllegalArgumentException("Event ID is required");
        }

        Optional<Team> existingTeam = teamRepository.findByEventIdAndTeamName(
            request.getEventId(), request.getTeamName());
        if (existingTeam.isPresent()) {
            throw new IllegalArgumentException("Team with name '" + request.getTeamName() +
                "' already exists for this event");
        }

        // Validate subscription limits for team capacity
        validateTeamCapacityLimits(request.getEventId(), request.getQuantity());

        Team team = teamMapper.toEntity(request);

        Event event = new Event();
        event.setEventId(request.getEventId());
        team.setEvent(event);
        team.setCreatedAt(LocalDateTime.now());

        Team savedTeam = teamRepository.save(team);
        logger.info("Team created successfully with ID: {}", savedTeam.getTeamId());

        return teamMapper.toTeamResponse(savedTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(UUID id) {
        logger.info("Getting team by ID: {}", id);

        Team team = teamRepository.findByIdWithMembers(id)
            .orElseThrow(() -> new RuntimeException("Team not found with ID: " + id));

        return teamMapper.toTeamResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getAll() {
        logger.info("Getting all teams");

        List<Team> teams = teamRepository.findAllWithMembers();
        return teams.stream()
            .map(teamMapper::toTeamResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getByEventId(UUID eventId) {
        logger.info("Getting teams by event ID: {}", eventId);

        List<Team> teams = teamRepository.findByEventIdWithMembers(eventId);
        return teams.stream()
            .map(teamMapper::toTeamResponse)
            .toList();
    }

    @Override
    @Transactional
    public TeamResponse update(UUID id, TeamRequest request) {
        logger.info("Updating team with ID: {}", id);

        Team existingTeam = teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Team not found with ID: " + id));

        if (request.getTeamName() != null && !request.getTeamName().trim().isEmpty()) {
            Optional<Team> duplicateTeam = teamRepository.findByEventIdAndTeamName(
                existingTeam.getEvent().getEventId(), request.getTeamName());
            if (duplicateTeam.isPresent() && !duplicateTeam.get().getTeamId().equals(id)) {
                throw new IllegalArgumentException("Team with name '" + request.getTeamName() +
                    "' already exists for this event");
            }
            existingTeam.setTeamName(request.getTeamName());
        }

        if (request.getQuantity() > 0) {
            int currentQuantity = existingTeam.getQuantity();
            int quantityDifference = request.getQuantity() - currentQuantity;

            if (quantityDifference > 0) {
                validateTeamCapacityLimits(existingTeam.getEvent().getEventId(), quantityDifference);
            }

            existingTeam.setQuantity(request.getQuantity());
        }

        Team updatedTeam = teamRepository.save(existingTeam);
        logger.info("Team updated successfully with ID: {}", updatedTeam.getTeamId());

        return teamMapper.toTeamResponse(updatedTeam);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        logger.info("Deleting team with ID: {}", id);

        if (!teamRepository.existsById(id)) {
            throw new RuntimeException("Team not found with ID: " + id);
        }

        teamRepository.deleteById(id);
        logger.info("Team deleted successfully with ID: {}", id);
    }

    private void validateTeamCapacityLimits(UUID eventId, int newTeamQuantity) {
        try {
            Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

            UUID eventCreatorId = event.getCreatedBy();
            if (eventCreatorId == null) {
                logger.warn("Event {} has no creator ID, skipping subscription validation", eventId);
                return;
            }

            int userLimit = subscriptionService.getRecruitmentLimitForUser(eventCreatorId);

            List<Team> existingTeams = teamRepository.findByEventId(eventId);
            int currentTotalCapacity = existingTeams.stream()
                .mapToInt(Team::getQuantity)
                .sum();

            int totalCapacityAfterAddingTeam = currentTotalCapacity + newTeamQuantity;

            if (totalCapacityAfterAddingTeam > userLimit) {
                String planName = getCurrentPlanName(eventCreatorId);
                throw new IllegalArgumentException(
                    String.format("Adding this team would exceed the event creator's %s plan limit. " +
                        "Current total capacity: %d, New team capacity: %d, Total would be: %d, but limit is: %d. " +
                        "Please upgrade the subscription or reduce team size.",
                        planName, currentTotalCapacity, newTeamQuantity, totalCapacityAfterAddingTeam, userLimit));
            }

        } catch (Exception ex) {
            if (ex instanceof IllegalArgumentException) {
                throw ex;
            }
            logger.error("Error validating team capacity limits: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to validate team capacity limits: " + ex.getMessage());
        }
    }

    private String getCurrentPlanName(UUID userId) {
        try {
            return subscriptionService.getUserCurrentPlan(userId).getPlanName();
        } catch (Exception e) {
            return "Free";
        }
    }
}