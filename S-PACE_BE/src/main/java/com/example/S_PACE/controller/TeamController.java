package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.TeamRequest;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.dto.response.TeamResponse;
import com.example.S_PACE.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Team Management", description = "Team management APIs")
public class TeamController {

    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

    @Autowired
    private TeamService teamService;

    @PostMapping
    @Operation(summary = "Create a new team", description = "Create a new team for an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Team created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Team already exists")
    })
    public ResponseEntity<ResponseDTO<TeamResponse>> createTeam(@RequestBody TeamRequest request) {
        try {
            logger.info("Creating team request received for: {}", request.getTeamName());
            TeamResponse response = teamService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Team created successfully", response));
        } catch (IllegalArgumentException ex) {
            logger.warn("Team creation validation error: {}", ex.getMessage());
            HttpStatus status = ex.getMessage().contains("already exists") ?
                HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Team creation failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Team creation failed: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID", description = "Retrieve a specific team by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team found"),
        @ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ResponseDTO<TeamResponse>> getTeamById(@PathVariable UUID id) {
        try {
            logger.info("Getting team by ID: {}", id);
            TeamResponse response = teamService.getById(id);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Team retrieved successfully", response));
        } catch (RuntimeException ex) {
            logger.warn("Team not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Failed to get team: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to get team: " + ex.getMessage(), null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all teams", description = "Retrieve all teams")
    @ApiResponse(responseCode = "200", description = "Teams retrieved successfully")
    public ResponseEntity<ResponseDTO<List<TeamResponse>>> getAllTeams() {
        try {
            logger.info("Getting all teams");
            List<TeamResponse> response = teamService.getAll();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Teams retrieved successfully", response));
        } catch (Exception ex) {
            logger.error("Failed to get teams: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to get teams: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get teams by event", description = "Retrieve all teams for a specific event")
    @ApiResponse(responseCode = "200", description = "Teams retrieved successfully")
    public ResponseEntity<ResponseDTO<List<TeamResponse>>> getTeamsByEvent(@PathVariable UUID eventId) {
        try {
            logger.info("Getting teams for event ID: {}", eventId);
            List<TeamResponse> response = teamService.getByEventId(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Teams retrieved successfully", response));
        } catch (Exception ex) {
            logger.error("Failed to get teams for event: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to get teams: " + ex.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update team", description = "Update an existing team")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Team not found"),
        @ApiResponse(responseCode = "409", description = "Team name conflict")
    })
    public ResponseEntity<ResponseDTO<TeamResponse>> updateTeam(
            @PathVariable UUID id,
            @RequestBody TeamRequest request) {
        try {
            logger.info("Updating team with ID: {}", id);
            TeamResponse response = teamService.update(id, request);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Team updated successfully", response));
        } catch (IllegalArgumentException ex) {
            logger.warn("Team update validation error: {}", ex.getMessage());
            HttpStatus status = ex.getMessage().contains("already exists") ?
                HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (RuntimeException ex) {
            logger.warn("Team not found for update: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Team update failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Team update failed: " + ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete team", description = "Delete a team by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ResponseDTO<Void>> deleteTeam(@PathVariable UUID id) {
        try {
            logger.info("Deleting team with ID: {}", id);
            teamService.delete(id);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Team deleted successfully", null));
        } catch (RuntimeException ex) {
            logger.warn("Team not found for deletion: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Team deletion failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Team deletion failed: " + ex.getMessage(), null));
        }
    }
}