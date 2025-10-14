package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.CertificateRequest;
import com.example.S_PACE.dto.response.CertificateResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.pojo.Certificates;
import com.example.S_PACE.service.CertificateService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificates")
@Tag(name = "Certificate Management", description = "Certificate management APIs")
public class CertificateController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create certificate with file upload",
        description = "Create a new certificate for a user in an event with file upload"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Certificate created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or file"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<CertificateResponse>> createCertificate(
            @RequestParam("eventId") UUID eventId,
            @RequestParam("userId") UUID userId,
            @RequestParam("certificateCode") String certificateCode,
            @RequestParam("issuedDate") String issuedDate,
            @RequestParam(value = "issuedBy", required = false) String issuedBy,
            @RequestParam(value = "certificate", required = false) MultipartFile certificateFile) {
        try {
            logger.info("Creating certificate for user: {} in event: {}", userId, eventId);

            String filePath = null;
            if (certificateFile != null && !certificateFile.isEmpty()) {
                logger.info("Uploading certificate file");
                filePath = fileUploadService.uploadCertificate(certificateFile, userId);
                logger.info("Certificate file uploaded: {}", filePath);
            }

            CertificateRequest request = new CertificateRequest();
            request.setEventId(eventId);
            request.setUserId(userId);
            request.setCertificateCode(certificateCode);
            request.setIssuedDate(LocalDate.parse(issuedDate));
            request.setIssuedBy(issuedBy);

            Certificates certificate = certificateService.createCertificate(request, filePath);
            CertificateResponse response = convertToResponse(certificate);

            logger.info("Certificate created successfully with ID: {}", certificate.getCertificatesId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Certificate created successfully", response));

        } catch (IllegalArgumentException ex) {
            logger.warn("Certificate creation validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IOException ex) {
            logger.error("File upload error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload certificate file: " + ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error creating certificate: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to create certificate", null));
        }
    }

    @GetMapping("/{certificateId}")
    @Operation(summary = "Get certificate by ID", description = "Retrieve a specific certificate by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Certificate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER', 'TEAM_LEADER', 'EMPLOYEE', 'COLLABORATOR')")
    public ResponseEntity<ResponseDTO<CertificateResponse>> getCertificateById(@PathVariable UUID certificateId) {
        try {
            logger.info("Fetching certificate by ID: {}", certificateId);
            Certificates certificate = certificateService.getCertificateById(certificateId);
            CertificateResponse response = convertToResponse(certificate);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificate retrieved successfully", response));
        } catch (IllegalArgumentException ex) {
            logger.warn("Certificate not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching certificate {}: {}", certificateId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch certificate", null));
        }
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get certificates by event", description = "Retrieve all certificates for a specific event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<List<CertificateResponse>>> getCertificatesByEvent(@PathVariable UUID eventId) {
        try {
            logger.info("Fetching certificates for event: {}", eventId);
            List<Certificates> certificates = certificateService.getCertificatesByEvent(eventId);
            List<CertificateResponse> responses = certificates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificates retrieved successfully", responses));
        } catch (Exception ex) {
            logger.error("Error fetching certificates for event {}: {}", eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch certificates", null));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get certificates by user", description = "Retrieve all certificates for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER') or #userId == authentication.principal.userId")
    public ResponseEntity<ResponseDTO<List<CertificateResponse>>> getCertificatesByUser(@PathVariable UUID userId) {
        try {
            logger.info("Fetching certificates for user: {}", userId);
            List<Certificates> certificates = certificateService.getCertificatesByUser(userId);
            List<CertificateResponse> responses = certificates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificates retrieved successfully", responses));
        } catch (Exception ex) {
            logger.error("Error fetching certificates for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch certificates", null));
        }
    }

    @GetMapping("/user/my-certificates")
    @Operation(summary = "Get my certificates", description = "Retrieve all certificates for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER', 'TEAM_LEADER', 'EMPLOYEE', 'COLLABORATOR')")
    public ResponseEntity<ResponseDTO<List<CertificateResponse>>> getMyCertificates(HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromToken(request);
            logger.info("Fetching certificates for current user: {}", userId);
            List<Certificates> certificates = certificateService.getCertificatesByUser(userId);
            List<CertificateResponse> responses = certificates.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificates retrieved successfully", responses));
        } catch (Exception ex) {
            logger.error("Error fetching certificates for current user: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch certificates", null));
        }
    }

    @GetMapping("/event/{eventId}/user/{userId}")
    @Operation(summary = "Get certificate by event and user", description = "Retrieve certificate for a specific user in an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Certificate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER') or #userId == authentication.principal.userId")
    public ResponseEntity<ResponseDTO<CertificateResponse>> getCertificateByEventAndUser(
            @PathVariable UUID eventId,
            @PathVariable UUID userId) {
        try {
            logger.info("Fetching certificate for user: {} in event: {}", userId, eventId);
            Certificates certificate = certificateService.getCertificateByEventAndUser(eventId, userId);
            CertificateResponse response = convertToResponse(certificate);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificate retrieved successfully", response));
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

    @PutMapping(value = "/{certificateId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update certificate", description = "Update an existing certificate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Certificate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<CertificateResponse>> updateCertificate(
            @PathVariable UUID certificateId,
            @RequestParam("eventId") UUID eventId,
            @RequestParam("userId") UUID userId,
            @RequestParam("certificateCode") String certificateCode,
            @RequestParam("issuedDate") String issuedDate,
            @RequestParam(value = "issuedBy", required = false) String issuedBy,
            @RequestParam(value = "certificate", required = false) MultipartFile certificateFile) {
        try {
            logger.info("Updating certificate with ID: {}", certificateId);

            String filePath = null;
            if (certificateFile != null && !certificateFile.isEmpty()) {
                logger.info("Uploading new certificate file");
                filePath = fileUploadService.uploadCertificate(certificateFile, userId);
                logger.info("Certificate file uploaded: {}", filePath);
            }

            CertificateRequest request = new CertificateRequest();
            request.setEventId(eventId);
            request.setUserId(userId);
            request.setCertificateCode(certificateCode);
            request.setIssuedDate(LocalDate.parse(issuedDate));
            request.setIssuedBy(issuedBy);

            Certificates certificate = certificateService.updateCertificate(certificateId, request, filePath);
            CertificateResponse response = convertToResponse(certificate);

            logger.info("Certificate updated successfully with ID: {}", certificate.getCertificatesId());
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificate updated successfully", response));

        } catch (IllegalArgumentException ex) {
            logger.warn("Certificate update validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IOException ex) {
            logger.error("File upload error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload certificate file: " + ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error updating certificate {}: {}", certificateId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to update certificate", null));
        }
    }

    @DeleteMapping("/{certificateId}")
    @Operation(summary = "Delete certificate", description = "Delete a certificate permanently")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Certificate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> deleteCertificate(@PathVariable UUID certificateId) {
        try {
            logger.info("Deleting certificate with ID: {}", certificateId);
            certificateService.deleteCertificate(certificateId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificate deleted successfully", null));
        } catch (IllegalArgumentException ex) {
            logger.warn("Certificate deletion error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error deleting certificate {}: {}", certificateId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to delete certificate", null));
        }
    }

    @GetMapping("/check/{eventId}/{userId}")
    @Operation(summary = "Check if certificate exists", description = "Check if certificate exists for user in event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate existence checked successfully")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER', 'TEAM_LEADER', 'EMPLOYEE', 'COLLABORATOR')")
    public ResponseEntity<ResponseDTO<Boolean>> checkCertificateExists(
            @PathVariable UUID eventId,
            @PathVariable UUID userId) {
        try {
            logger.info("Checking if certificate exists for user: {} in event: {}", userId, eventId);
            boolean exists = certificateService.existsByEventAndUser(eventId, userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Certificate existence checked", exists));
        } catch (Exception ex) {
            logger.error("Error checking certificate existence: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to check certificate existence", false));
        }
    }

    @GetMapping("/debug/auth")
    @Operation(summary = "Debug authentication", description = "Test endpoint to check current user authentication")
    public ResponseEntity<ResponseDTO<Object>> debugAuth(HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromToken(request);
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

            java.util.Map<String, Object> debugInfo = new java.util.HashMap<>();
            debugInfo.put("userId", userId);
            debugInfo.put("authenticated", auth != null && auth.isAuthenticated());
            debugInfo.put("principal", auth != null ? auth.getPrincipal().toString() : "null");
            debugInfo.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");

            return ResponseEntity.ok(new ResponseDTO<>(true, "Auth debug info", debugInfo));
        } catch (Exception ex) {
            return ResponseEntity.ok(new ResponseDTO<>(false, "Auth error: " + ex.getMessage(), null));
        }
    }

    private CertificateResponse convertToResponse(Certificates certificate) {
        try {
            return CertificateResponse.builder()
                .certificatesId(certificate.getCertificatesId())
                .eventId(certificate.getEvent() != null ? certificate.getEvent().getEventId() : null)
                .eventName(certificate.getEvent() != null ? certificate.getEvent().getTitle() : null)
                .userId(certificate.getUser() != null ? certificate.getUser().getUserId() : null)
                .userName(certificate.getUser() != null ? certificate.getUser().getFullName() : null)
                .certificateFilePath(certificate.getCertificateFilePath())
                .certificateCode(certificate.getCertificateCode())
                .issuedDate(certificate.getIssuedDate())
                .issuedBy(certificate.getIssuedBy())
                .build();
        } catch (Exception ex) {
            logger.error("Error converting certificate to response: {}", ex.getMessage(), ex);
            // Return basic info if lazy loading fails
            return CertificateResponse.builder()
                .certificatesId(certificate.getCertificatesId())
                .certificateFilePath(certificate.getCertificateFilePath())
                .certificateCode(certificate.getCertificateCode())
                .issuedDate(certificate.getIssuedDate())
                .issuedBy(certificate.getIssuedBy())
                .build();
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
