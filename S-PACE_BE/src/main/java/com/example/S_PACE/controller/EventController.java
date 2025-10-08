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
import org.springframework.http.MediaType;

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

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create new event with image", 
        description = "Create a new event for the company with optional image upload. Accepts image files from user's computer (JPEG, PNG, GIF, WebP). Maximum file size: 5MB. Image is stored in AWS S3 cloud storage."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Event created successfully with image uploaded to AWS S3"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or file"),
        @ApiResponse(responseCode = "403", description = "Access denied - Event Manager role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Event data and optional image file",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "multipart/form-data"
        )
    )
    @PreAuthorize("hasRole('EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<EventResponse>> createEventWithImage(
            @RequestParam("eventName") String eventName,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("location") String location,
            @RequestParam(value = "maxParticipants", required = false) Integer maxParticipants,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "requirements", required = false) String requirements,
            @RequestParam(value = "contactInfo", required = false) String contactInfo,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam UUID companyId) {
        try {
            logger.info("Creating new event: {} for company: {}", eventName, companyId);
            
            // Validate required fields
            if (eventName == null || eventName.trim().isEmpty()) {
                throw new IllegalArgumentException("Event name cannot be null or empty");
            }
            
            if (companyId == null) {
                throw new IllegalArgumentException("Company ID cannot be null");
            }
            
            // Upload image if provided
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.info("Uploading event image");
                imageUrl = fileUploadService.uploadEventImage(imageFile);
                logger.info("Event image uploaded to S3: {}", imageUrl);
            }
            
            // Create EventRequest object
            EventRequest eventRequest = new EventRequest();
            eventRequest.setEventName(eventName);
            eventRequest.setDescription(description);
            eventRequest.setStartDate(java.time.LocalDate.parse(startDate));
            eventRequest.setEndDate(java.time.LocalDate.parse(endDate));
            eventRequest.setLocation(location);
            eventRequest.setMaxParticipants(maxParticipants);
            eventRequest.setRequirements(requirements);
            eventRequest.setContactInfo(contactInfo);
            eventRequest.setPicture(imageUrl); // Set the S3 URL
            
            // Parse status if provided
            if (status != null && !status.trim().isEmpty()) {
                try {
                    eventRequest.setStatus(EventStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid status: {}, using default", status);
                    eventRequest.setStatus(EventStatus.DRAFT);
                }
            } else {
                eventRequest.setStatus(EventStatus.DRAFT);
            }
            
            // Create event
            EventResponse createdEvent = eventService.createEvent(eventRequest, companyId);
            
            logger.info("Event created successfully with ID: {}", createdEvent.getEventId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Event created successfully", createdEvent));
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Event creation validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IOException ex) {
            logger.error("File upload error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload event image: " + ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error creating event: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to create event", null));
        }
    }

    @PostMapping
    @Operation(summary = "Create new event (JSON)", description = "Create a new event for the company without image upload (JSON format)")
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

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload event image", 
        description = "Upload event image to AWS S3. Accepts image files from user's computer (JPEG, PNG, GIF, WebP). Maximum file size: 5MB. File is stored in AWS S3 cloud storage."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event image uploaded successfully to AWS S3. Returns S3 URL."),
        @ApiResponse(responseCode = "400", description = "Invalid file (wrong type, too large, or empty)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required"),
        @ApiResponse(responseCode = "403", description = "Access denied - Event Manager role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Event image file",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "multipart/form-data"
        )
    )
    @PreAuthorize("hasRole('EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<String>> uploadEventImage(
            @RequestParam("image") MultipartFile imageFile,
            HttpServletRequest request) {
        try {
            logger.info("Uploading event image");
            
            // Validate file
            if (imageFile.isEmpty()) {
                logger.warn("Event image upload failed: File is empty");
                return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(false, "File is empty", null));
            }
            
            // Upload the event image to AWS S3
            String imageUrl = fileUploadService.uploadEventImage(imageFile);
            logger.info("Event image uploaded to S3: {}", imageUrl);
            
            logger.info("Event image uploaded successfully");
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event image uploaded successfully", imageUrl));
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Event image upload validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IOException ex) {
            logger.error("File upload error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload event image: " + ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error uploading event image: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload event image", null));
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