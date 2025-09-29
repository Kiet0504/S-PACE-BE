package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.EventRegisterRequest;
import com.example.S_PACE.dto.response.EventRegistrationResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.enums.EventRegistrationStatus;
import com.example.S_PACE.exception.AuthenticationException;
import com.example.S_PACE.service.EventRegistrationService;
import com.example.S_PACE.utils.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/event-registrations")
@Tag(name = "Event Registration Management", description = "Event registration APIs for collaborators")
public class EventRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(EventRegistrationController.class);

    @Autowired
    private EventRegistrationService eventRegistrationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @Operation(summary = "Register for an event", description = "Allows collaborators to register for events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Only collaborators can register"),
        @ApiResponse(responseCode = "409", description = "Already registered for this event")
    })
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<ResponseDTO<EventRegistrationResponse>> registerForEvent(
            @Valid @RequestBody EventRegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromToken(httpRequest);
            logger.info("Event registration request received from user: {} for event: {}", userId, request.getEventId());
            
            EventRegistrationResponse response = eventRegistrationService.registerForEvent(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Registration submitted successfully", response));
                
        } catch (AuthenticationException ex) {
            logger.warn("Authentication error during registration: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IllegalArgumentException ex) {
            logger.warn("Registration validation error: {}", ex.getMessage());
            HttpStatus status = ex.getMessage().contains("already registered") ? 
                HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Unexpected error during registration: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Registration failed", null));
        }
    }

    @PutMapping("/{registrationId}/status")
    @Operation(summary = "Update registration status", description = "Update the status of an event registration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'COMPANY_ADMIN', 'ADMIN')")
    public ResponseEntity<ResponseDTO<EventRegistrationResponse>> updateRegistrationStatus(
            @PathVariable UUID registrationId,
            @RequestParam EventRegistrationStatus status,
            @RequestParam(required = false) String reviewNotes,
            HttpServletRequest httpRequest) {
        try {
            UUID reviewerId = getUserIdFromToken(httpRequest);
            logger.info("Updating registration status for ID: {} to status: {}", registrationId, status);
            
            EventRegistrationResponse response = eventRegistrationService.updateRegistrationStatus(
                registrationId, status, reviewNotes, reviewerId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Status updated successfully", response));
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Status update validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Unexpected error during status update: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Status update failed", null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all registrations", description = "Retrieve all event registrations")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'COMPANY_ADMIN', 'ADMIN')")
    public ResponseEntity<ResponseDTO<List<EventRegistrationResponse>>> getAllRegistrations() {
        try {
            logger.info("Fetching all event registrations");
            List<EventRegistrationResponse> registrations = eventRegistrationService.getAllRegistrations();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registrations retrieved successfully", registrations));
        } catch (Exception ex) {
            logger.error("Error fetching all registrations: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch registrations", null));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get registrations by user ID", description = "Retrieve registrations for a specific user")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'COMPANY_ADMIN', 'ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ResponseDTO<List<EventRegistrationResponse>>> getRegistrationsByUserId(@PathVariable UUID userId) {
        try {
            logger.info("Fetching registrations for user: {}", userId);
            List<EventRegistrationResponse> registrations = eventRegistrationService.getRegistrationsByUserId(userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User registrations retrieved successfully", registrations));
        } catch (Exception ex) {
            logger.error("Error fetching registrations for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch user registrations", null));
        }
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get registrations by event ID", description = "Retrieve registrations for a specific event")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'COMPANY_ADMIN', 'ADMIN')")
    public ResponseEntity<ResponseDTO<List<EventRegistrationResponse>>> getRegistrationsByEventId(@PathVariable UUID eventId) {
        try {
            logger.info("Fetching registrations for event: {}", eventId);
            List<EventRegistrationResponse> registrations = eventRegistrationService.getRegistrationsByEventId(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event registrations retrieved successfully", registrations));
        } catch (Exception ex) {
            logger.error("Error fetching registrations for event {}: {}", eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch event registrations", null));
        }
    }

    @GetMapping("/{registrationId}")
    @Operation(summary = "Get registration by ID", description = "Retrieve a specific registration by its ID")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'COMPANY_ADMIN', 'ADMIN', 'COLLABORATOR')")
    public ResponseEntity<ResponseDTO<EventRegistrationResponse>> getRegistrationById(@PathVariable UUID registrationId) {
        try {
            logger.info("Fetching registration by ID: {}", registrationId);
            EventRegistrationResponse registration = eventRegistrationService.getRegistrationById(registrationId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registration retrieved successfully", registration));
        } catch (IllegalArgumentException ex) {
            logger.warn("Registration not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching registration {}: {}", registrationId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch registration", null));
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get registrations by status", description = "Retrieve registrations with a specific status")
    @PreAuthorize("hasAnyRole('EVENT_MANAGER', 'COMPANY_ADMIN', 'ADMIN')")
    public ResponseEntity<ResponseDTO<List<EventRegistrationResponse>>> getRegistrationsByStatus(@PathVariable EventRegistrationStatus status) {
        try {
            logger.info("Fetching registrations with status: {}", status);
            List<EventRegistrationResponse> registrations = eventRegistrationService.getRegistrationsByStatus(status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registrations retrieved successfully", registrations));
        } catch (Exception ex) {
            logger.error("Error fetching registrations with status {}: {}", status, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch registrations", null));
        }
    }

    @GetMapping("/my-registrations")
    @Operation(summary = "Get current user's registrations", description = "Retrieve registrations for the currently authenticated user")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<ResponseDTO<List<EventRegistrationResponse>>> getMyRegistrations(HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromToken(httpRequest);
            logger.info("Fetching registrations for current user: {}", userId);
            List<EventRegistrationResponse> registrations = eventRegistrationService.getRegistrationsByUserId(userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Your registrations retrieved successfully", registrations));
        } catch (Exception ex) {
            logger.error("Error fetching current user's registrations: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch your registrations", null));
        }
    }

    @PutMapping("/{registrationId}/cancel")
    @Operation(summary = "Cancel registration", description = "Cancel your own event registration")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<ResponseDTO<EventRegistrationResponse>> cancelRegistration(
            @PathVariable UUID registrationId,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromToken(httpRequest);
            logger.info("Cancelling registration: {} for user: {}", registrationId, userId);
            
            EventRegistrationResponse response = eventRegistrationService.cancelRegistration(registrationId, userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Registration cancelled successfully", response));
            
        } catch (AuthenticationException ex) {
            logger.warn("Authentication error during cancellation: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IllegalArgumentException ex) {
            logger.warn("Cancellation validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Unexpected error during cancellation: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Cancellation failed", null));
        }
    }

    private UUID getUserIdFromToken(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (StringUtils.hasText(token)) {
            String userIdStr = jwtTokenProvider.getUserIdFromJWT(token);
            return UUID.fromString(userIdStr);
        }
        throw new AuthenticationException("Invalid or missing token");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
