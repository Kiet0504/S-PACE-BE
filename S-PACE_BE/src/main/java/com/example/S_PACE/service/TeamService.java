package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.TeamRequest;
import com.example.S_PACE.dto.response.TeamResponse;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    TeamResponse create(TeamRequest request);
    TeamResponse getById(UUID id);
    List<TeamResponse> getAll();
    List<TeamResponse> getByEventId(UUID eventId);
    TeamResponse update(UUID id, TeamRequest request);
    void delete(UUID id);
}