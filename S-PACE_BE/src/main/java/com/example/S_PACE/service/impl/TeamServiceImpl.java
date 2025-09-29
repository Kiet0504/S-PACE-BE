package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.TeamRequest;
import com.example.S_PACE.dto.response.TeamResponse;
import com.example.S_PACE.mapper.TeamMapper;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.Team;
import com.example.S_PACE.repository.TeamRepository;
import com.example.S_PACE.service.TeamService;
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
}