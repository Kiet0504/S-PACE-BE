package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.CertificateRequest;
import com.example.S_PACE.dto.request.EventRequest;
import com.example.S_PACE.dto.response.EventResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.enums.EventStatus;
import com.example.S_PACE.pojo.Certificates;
import com.example.S_PACE.service.CertificateService;
import com.example.S_PACE.service.EventService;
import com.example.S_PACE.service.FileUploadService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "Event management APIs")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "Create new event", description = "Create a new event for the company with optional picture")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Event created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied - Event Manager role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest eventRequest,
            @RequestParam UUID companyId) {
        try {
            logger.info("Creating new event: {} for company: {}", eventRequest.getEventName(), companyId);
            EventResponse createdEvent = eventService.createEvent(eventRequest, companyId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Event created successfully", createdEvent));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event creation validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error creating event: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to create event", null));
        }
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event by ID", description = "Retrieve a specific event by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<EventResponse>> getEventById(@PathVariable UUID eventId) {
        try {
            logger.info("Fetching event by ID: {}", eventId);
            EventResponse event = eventService.getEventById(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event retrieved successfully", event));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching event {}: {}", eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch event", null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieve all events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<List<EventResponse>>> getAllEvents() {
        try {
            logger.info("Fetching all events");
            List<EventResponse> events = eventService.getAllEvents();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Events retrieved successfully", events));
        } catch (Exception ex) {
            logger.error("Error fetching all events: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch events", null));
        }
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get events by company", description = "Retrieve all events for a specific company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<List<EventResponse>>> getEventsByCompany(@PathVariable UUID companyId) {
        try {
            logger.info("Fetching events for company: {}", companyId);
            List<EventResponse> events = eventService.getEventsByCompany(companyId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Events retrieved successfully", events));
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid company ID: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching events for company {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch events", null));
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get events by status", description = "Retrieve events with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<List<EventResponse>>> getEventsByStatus(@PathVariable EventStatus status) {
        try {
            logger.info("Fetching events by status: {}", status);
            List<EventResponse> events = eventService.getEventsByStatus(status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Events retrieved successfully", events));
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid status: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching events by status {}: {}", status, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch events", null));
        }
    }

    @GetMapping("/company/{companyId}/status/{status}")
    @Operation(summary = "Get events by company and status", description = "Retrieve events for a company with specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<List<EventResponse>>> getEventsByCompanyAndStatus(
            @PathVariable UUID companyId,
            @PathVariable EventStatus status) {
        try {
            logger.info("Fetching events for company: {} with status: {}", companyId, status);
            List<EventResponse> events = eventService.getEventsByCompanyAndStatus(companyId, status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Events retrieved successfully", events));
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid parameters: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching events for company {} with status {}: {}", companyId, status, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch events", null));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search events by name", description = "Search events by name (case-insensitive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<List<EventResponse>>> searchEventsByName(@RequestParam String eventName) {
        try {
            logger.info("Searching events by name: {}", eventName);
            List<EventResponse> events = eventService.searchEventsByName(eventName);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Events retrieved successfully", events));
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid search query: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error searching events by name {}: {}", eventName, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to search events", null));
        }
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update event", description = "Update an existing event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<EventResponse>> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody EventRequest eventRequest) {
        try {
            logger.info("Updating event with ID: {}", eventId);
            EventResponse updatedEvent = eventService.updateEvent(eventId, eventRequest);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event updated successfully", updatedEvent));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event update validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error updating event {}: {}", eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to update event", null));
        }
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete event", description = "Delete an event permanently")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> deleteEvent(@PathVariable UUID eventId) {
        try {
            logger.info("Deleting event with ID: {}", eventId);
            eventService.deleteEvent(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event deleted successfully", null));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event deletion error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error deleting event {}: {}", eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to delete event", null));
        }
    }

    @GetMapping("/{eventId}/exists")
    @Operation(summary = "Check if event exists", description = "Check if an event exists by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event existence checked successfully")
    })
    public ResponseEntity<ResponseDTO<Boolean>> checkEventExists(@PathVariable UUID eventId) {
        try {
            logger.info("Checking if event exists with ID: {}", eventId);
            boolean exists = eventService.existsById(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event existence checked", exists));
        } catch (Exception ex) {
            logger.error("Error checking event existence {}: {}", eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to check event existence", false));
        }
    }

    @PostMapping("/{eventId}/certificates/{userId}")
    @Operation(summary = "Upload certificate for event participant", description = "Upload certificate for a specific user in an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or user/event not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER') or #userId == authentication.principal.userId")
    public ResponseEntity<ResponseDTO<Certificates>> uploadEventCertificate(
            @PathVariable UUID eventId,
            @PathVariable UUID userId,
            @RequestParam("certificate") MultipartFile certificateFile,
            @RequestParam("certificateCode") String certificateCode,
            @RequestParam("issuedDate") String issuedDate,
            @RequestParam(value = "issuedBy", required = false) String issuedBy,
            HttpServletRequest request) {
        try {
            logger.info("Uploading certificate for user: {} in event: {}", userId, eventId);
            
            // Verify event exists
            eventService.getEventById(eventId);
            
            // Upload the certificate file
            String certificatePath = fileUploadService.uploadCertificate(certificateFile, userId);
            
            // Create certificate request
            CertificateRequest certificateRequest = new CertificateRequest();
            certificateRequest.setEventId(eventId);
            certificateRequest.setUserId(userId);
            certificateRequest.setCertificateCode(certificateCode);
            certificateRequest.setIssuedDate(java.time.LocalDate.parse(issuedDate));
            certificateRequest.setIssuedBy(issuedBy);
            
            // Save certificate to database
            Certificates savedCertificate = certificateService.createCertificate(certificateRequest, certificatePath);
            
            logger.info("Certificate uploaded and saved successfully for user: {} in event: {}", userId, eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificate uploaded successfully", savedCertificate));
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Certificate upload validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IOException ex) {
            logger.error("File upload error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload certificate", null));
        } catch (Exception ex) {
            logger.error("Error uploading certificate: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload certificate", null));
        }
    }

    @PostMapping("/{eventId}/certificates/my-profile")
    @Operation(summary = "Upload my certificate for event", description = "Upload certificate for the current user in a specific event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or event not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER', 'TEAM_LEADER', 'EMPLOYEE', 'COLLABORATOR')")
    public ResponseEntity<ResponseDTO<Certificates>> uploadMyEventCertificate(
            @PathVariable UUID eventId,
            @RequestParam("certificate") MultipartFile certificateFile,
            @RequestParam("certificateCode") String certificateCode,
            @RequestParam("issuedDate") String issuedDate,
            @RequestParam(value = "issuedBy", required = false) String issuedBy,
            HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromToken(request);
            logger.info("User {} uploading certificate for event: {}", userId, eventId);
            
            // Verify event exists
            eventService.getEventById(eventId);
            
            // Upload the certificate file
            String certificatePath = fileUploadService.uploadCertificate(certificateFile, userId);
            
            // Create certificate request
            CertificateRequest certificateRequest = new CertificateRequest();
            certificateRequest.setEventId(eventId);
            certificateRequest.setUserId(userId);
            certificateRequest.setCertificateCode(certificateCode);
            certificateRequest.setIssuedDate(java.time.LocalDate.parse(issuedDate));
            certificateRequest.setIssuedBy(issuedBy);
            
            // Save certificate to database
            Certificates savedCertificate = certificateService.createCertificate(certificateRequest, certificatePath);
            
            logger.info("Certificate uploaded and saved successfully for user: {} in event: {}", userId, eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificate uploaded successfully", savedCertificate));
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Certificate upload validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IOException ex) {
            logger.error("File upload error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload certificate", null));
        } catch (Exception ex) {
            logger.error("Error uploading certificate: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload certificate", null));
        }
    }

    @GetMapping("/{eventId}/certificates")
    @Operation(summary = "Get certificates for event", description = "Get all certificates for a specific event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<List<Certificates>>> getEventCertificates(@PathVariable UUID eventId) {
        try {
            logger.info("Fetching certificates for event: {}", eventId);
            List<Certificates> certificates = certificateService.getCertificatesByEvent(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificates retrieved successfully", certificates));
        } catch (Exception ex) {
            logger.error("Error fetching certificates for event {}: {}", eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch certificates", null));
        }
    }

    @GetMapping("/{eventId}/certificates/{userId}")
    @Operation(summary = "Get user certificate for event", description = "Get certificate for a specific user in an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Certificate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER') or #userId == authentication.principal.userId")
    public ResponseEntity<ResponseDTO<Certificates>> getUserEventCertificate(
            @PathVariable UUID eventId,
            @PathVariable UUID userId) {
        try {
            logger.info("Fetching certificate for user: {} in event: {}", userId, eventId);
            Certificates certificate = certificateService.getCertificateByEventAndUser(eventId, userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificate retrieved successfully", certificate));
        } catch (IllegalArgumentException ex) {
            logger.warn("Certificate not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching certificate for user {} in event {}: {}", userId, eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch certificate", null));
        }
    }

    private UUID getUserIdFromToken(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (StringUtils.hasText(token)) {
            String userIdStr = jwtTokenProvider.getUserIdFromJWT(token);
            return UUID.fromString(userIdStr);
        }
        throw new IllegalArgumentException("Invalid or missing token");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}