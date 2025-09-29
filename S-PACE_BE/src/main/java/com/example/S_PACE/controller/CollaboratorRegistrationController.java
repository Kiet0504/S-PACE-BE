package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.CollaboratorRegistrationRequest;
import com.example.S_PACE.dto.response.CollaboratorRegistrationResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.enums.EventRegistrationStatus;
import com.example.S_PACE.service.CollaboratorRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collaborator-registrations")
@Tag(name = "Collaborator Registration", description = "Collaborator registration management APIs")
@RequiredArgsConstructor
@Slf4j
public class CollaboratorRegistrationController {

    private final CollaboratorRegistrationService collaboratorRegistrationService;

    @PostMapping
    @Operation(summary = "Register collaborator", description = "Register a new collaborator for an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Collaborator registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "User already registered for this event")
    })
    public ResponseEntity<ResponseDTO<CollaboratorRegistrationResponse>> registerCollaborator(
            @Valid @RequestBody CollaboratorRegistrationRequest request) {
        try {
            log.info("Collaborator registration request received for event: {}", request.getEventId());
            CollaboratorRegistrationResponse response = collaboratorRegistrationService.registerCollaborator(request);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Collaborator registered successfully", response));
        } catch (IllegalArgumentException ex) {
            log.warn("Collaborator registration validation error: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("Collaborator registration failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Registration failed: " + ex.getMessage(), null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all registrations", description = "Get all collaborator registrations")
    public ResponseEntity<ResponseDTO<List<CollaboratorRegistrationResponse>>> getAllRegistrations() {
        try {
            List<CollaboratorRegistrationResponse> registrations = collaboratorRegistrationService.getAllRegistrations();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registrations retrieved successfully", registrations));
        } catch (Exception ex) {
            log.error("Failed to retrieve registrations: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to retrieve registrations: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get registrations by event", description = "Get all collaborator registrations for a specific event")
    public ResponseEntity<ResponseDTO<List<CollaboratorRegistrationResponse>>> getRegistrationsByEventId(
            @Parameter(description = "Event ID") @PathVariable UUID eventId) {
        try {
            List<CollaboratorRegistrationResponse> registrations = 
                    collaboratorRegistrationService.getRegistrationsByEventId(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event registrations retrieved successfully", registrations));
        } catch (Exception ex) {
            log.error("Failed to retrieve event registrations: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to retrieve event registrations: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get registrations by user", description = "Get all collaborator registrations for a specific user")
    public ResponseEntity<ResponseDTO<List<CollaboratorRegistrationResponse>>> getRegistrationsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        try {
            List<CollaboratorRegistrationResponse> registrations = 
                    collaboratorRegistrationService.getRegistrationsByUserId(userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User registrations retrieved successfully", registrations));
        } catch (Exception ex) {
            log.error("Failed to retrieve user registrations: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to retrieve user registrations: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/{registrationId}")
    @Operation(summary = "Get registration by ID", description = "Get a specific collaborator registration by ID")
    public ResponseEntity<ResponseDTO<CollaboratorRegistrationResponse>> getRegistrationById(
            @Parameter(description = "Registration ID") @PathVariable UUID registrationId) {
        try {
            CollaboratorRegistrationResponse registration = 
                    collaboratorRegistrationService.getRegistrationById(registrationId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registration retrieved successfully", registration));
        } catch (IllegalArgumentException ex) {
            log.warn("Registration not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("Failed to retrieve registration: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to retrieve registration: " + ex.getMessage(), null));
        }
    }

    @PutMapping("/{registrationId}/status")
    @Operation(summary = "Update registration status", description = "Update the status of a collaborator registration")
    public ResponseEntity<ResponseDTO<CollaboratorRegistrationResponse>> updateRegistrationStatus(
            @Parameter(description = "Registration ID") @PathVariable UUID registrationId,
            @Parameter(description = "New status") @RequestParam EventRegistrationStatus status,
            @Parameter(description = "Review notes") @RequestParam(required = false) String reviewNotes,
            @Parameter(description = "Reviewer ID") @RequestParam UUID reviewedBy) {
        try {
            CollaboratorRegistrationResponse response = 
                    collaboratorRegistrationService.updateRegistrationStatus(registrationId, status, reviewNotes, reviewedBy);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registration status updated successfully", response));
        } catch (IllegalArgumentException ex) {
            log.warn("Status update validation error: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("Failed to update registration status: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to update status: " + ex.getMessage(), null));
        }
    }

    @PutMapping("/{registrationId}/cancel")
    @Operation(summary = "Cancel registration", description = "Cancel a collaborator registration")
    public ResponseEntity<ResponseDTO<CollaboratorRegistrationResponse>> cancelRegistration(
            @Parameter(description = "Registration ID") @PathVariable UUID registrationId) {
        try {
            CollaboratorRegistrationResponse response = 
                    collaboratorRegistrationService.cancelRegistration(registrationId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registration cancelled successfully", response));
        } catch (IllegalArgumentException ex) {
            log.warn("Cancel registration validation error: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            log.error("Failed to cancel registration: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to cancel registration: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/event/{eventId}/count")
    @Operation(summary = "Get registration count", description = "Get the count of registrations for an event")
    public ResponseEntity<ResponseDTO<Long>> getRegistrationCount(
            @Parameter(description = "Event ID") @PathVariable UUID eventId) {
        try {
            long count = collaboratorRegistrationService.getRegistrationCountByEventId(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registration count retrieved successfully", count));
        } catch (Exception ex) {
            log.error("Failed to retrieve registration count: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to retrieve registration count: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/event/{eventId}/status/{status}/count")
    @Operation(summary = "Get registration count by status", description = "Get the count of registrations for an event by status")
    public ResponseEntity<ResponseDTO<Long>> getRegistrationCountByStatus(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Parameter(description = "Registration status") @PathVariable EventRegistrationStatus status) {
        try {
            long count = collaboratorRegistrationService.getRegistrationCountByEventIdAndStatus(eventId, status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registration count by status retrieved successfully", count));
        } catch (Exception ex) {
            log.error("Failed to retrieve registration count by status: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to retrieve registration count by status: " + ex.getMessage(), null));
        }
    }
} 