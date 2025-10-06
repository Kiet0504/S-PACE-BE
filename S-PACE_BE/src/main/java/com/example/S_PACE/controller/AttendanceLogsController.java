package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.AttendanceLogsRequest;
import com.example.S_PACE.dto.response.AttendanceLogsResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.enums.AttendanceStatus;
import com.example.S_PACE.service.AttendanceLogsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance-logs")
@Tag(name = "Attendance Logs Management", description = "Attendance logs management APIs")
public class AttendanceLogsController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceLogsController.class);

    @Autowired
    private AttendanceLogsService attendanceLogsService;

    @PostMapping
    @Operation(summary = "Create new attendance log", description = "Create a new attendance log")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Attendance log created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<AttendanceLogsResponse>> createAttendanceLog(
            @Valid @RequestBody AttendanceLogsRequest request) {
        try {
            logger.info("Creating new attendance log for user: {} and event: {}", request.getUserId(), request.getEventId());
            AttendanceLogsResponse createdLog = attendanceLogsService.createAttendanceLog(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Attendance log created successfully", createdLog));
        } catch (IllegalArgumentException ex) {
            logger.warn("Attendance log creation validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error creating attendance log: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to create attendance log", null));
        }
    }

    @GetMapping("/{attendanceLogsId}")
    @Operation(summary = "Get attendance log by ID", description = "Retrieve a specific attendance log by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance log retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Attendance log not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<AttendanceLogsResponse>> getAttendanceLogById(@PathVariable UUID attendanceLogsId) {
        try {
            logger.info("Fetching attendance log by ID: {}", attendanceLogsId);
            AttendanceLogsResponse log = attendanceLogsService.getAttendanceLogById(attendanceLogsId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Attendance log retrieved successfully", log));
        } catch (RuntimeException ex) {
            logger.warn("Attendance log not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching attendance log {}: {}", attendanceLogsId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch attendance log", null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all attendance logs", description = "Retrieve all attendance logs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance logs retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<List<AttendanceLogsResponse>>> getAllAttendanceLogs() {
        try {
            logger.info("Fetching all attendance logs");
            List<AttendanceLogsResponse> logs = attendanceLogsService.getAllAttendanceLogs();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Attendance logs retrieved successfully", logs));
        } catch (Exception ex) {
            logger.error("Error fetching all attendance logs: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch attendance logs", null));
        }
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get attendance logs by event", description = "Retrieve all attendance logs for a specific event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance logs retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<List<AttendanceLogsResponse>>> getAttendanceLogsByEventId(@PathVariable UUID eventId) {
        try {
            logger.info("Fetching attendance logs for event: {}", eventId);
            List<AttendanceLogsResponse> logs = attendanceLogsService.getAttendanceLogsByEventId(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Attendance logs retrieved successfully", logs));
        } catch (Exception ex) {
            logger.error("Error fetching attendance logs for event {}: {}", eventId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch attendance logs", null));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get attendance logs by user", description = "Retrieve all attendance logs for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance logs retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<List<AttendanceLogsResponse>>> getAttendanceLogsByUserId(@PathVariable UUID userId) {
        try {
            logger.info("Fetching attendance logs for user: {}", userId);
            List<AttendanceLogsResponse> logs = attendanceLogsService.getAttendanceLogsByUserId(userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Attendance logs retrieved successfully", logs));
        } catch (Exception ex) {
            logger.error("Error fetching attendance logs for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch attendance logs", null));
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get attendance logs by status", description = "Retrieve attendance logs with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance logs retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<List<AttendanceLogsResponse>>> getAttendanceLogsByStatus(@PathVariable AttendanceStatus status) {
        try {
            logger.info("Fetching attendance logs by status: {}", status);
            List<AttendanceLogsResponse> logs = attendanceLogsService.getAttendanceLogsByStatus(status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Attendance logs retrieved successfully", logs));
        } catch (Exception ex) {
            logger.error("Error fetching attendance logs by status {}: {}", status, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch attendance logs", null));
        }
    }

    @GetMapping("/event/{eventId}/status/{status}")
    @Operation(summary = "Get attendance logs by event and status", description = "Retrieve attendance logs for an event with specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance logs retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<List<AttendanceLogsResponse>>> getAttendanceLogsByEventIdAndStatus(
            @PathVariable UUID eventId,
            @PathVariable AttendanceStatus status) {
        try {
            logger.info("Fetching attendance logs for event: {} with status: {}", eventId, status);
            List<AttendanceLogsResponse> logs = attendanceLogsService.getAttendanceLogsByEventIdAndStatus(eventId, status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Attendance logs retrieved successfully", logs));
        } catch (Exception ex) {
            logger.error("Error fetching attendance logs for event {} with status {}: {}", eventId, status, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch attendance logs", null));
        }
    }

    @PutMapping("/{attendanceLogsId}")
    @Operation(summary = "Update attendance log", description = "Update an existing attendance log")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance log updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Attendance log not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<AttendanceLogsResponse>> updateAttendanceLog(
            @PathVariable UUID attendanceLogsId,
            @Valid @RequestBody AttendanceLogsRequest request) {
        try {
            logger.info("Updating attendance log with ID: {}", attendanceLogsId);
            AttendanceLogsResponse updatedLog = attendanceLogsService.updateAttendanceLog(attendanceLogsId, request);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Attendance log updated successfully", updatedLog));
        } catch (RuntimeException ex) {
            logger.warn("Attendance log update error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error updating attendance log {}: {}", attendanceLogsId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to update attendance log", null));
        }
    }

    @DeleteMapping("/{attendanceLogsId}")
    @Operation(summary = "Delete attendance log", description = "Delete an attendance log permanently")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attendance log deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Attendance log not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<Void>> deleteAttendanceLog(@PathVariable UUID attendanceLogsId) {
        try {
            logger.info("Deleting attendance log with ID: {}", attendanceLogsId);
            attendanceLogsService.deleteAttendanceLog(attendanceLogsId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Attendance log deleted successfully", null));
        } catch (RuntimeException ex) {
            logger.warn("Attendance log deletion error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error deleting attendance log {}: {}", attendanceLogsId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to delete attendance log", null));
        }
    }

    @PostMapping("/check-in")
    @Operation(summary = "Check in to event", description = "Check in a user to an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Checked in successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or already checked in"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<AttendanceLogsResponse>> checkIn(
            @RequestParam UUID eventId,
            @RequestParam UUID userId) {
        try {
            logger.info("User {} checking in to event {}", userId, eventId);
            AttendanceLogsResponse log = attendanceLogsService.checkIn(eventId, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Checked in successfully", log));
        } catch (RuntimeException ex) {
            logger.warn("Check in error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error checking in: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to check in", null));
        }
    }

    @PostMapping("/check-out/{attendanceLogsId}")
    @Operation(summary = "Check out from event", description = "Check out a user from an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Checked out successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or already checked out"),
        @ApiResponse(responseCode = "404", description = "Attendance log not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<AttendanceLogsResponse>> checkOut(@PathVariable UUID attendanceLogsId) {
        try {
            logger.info("Checking out attendance log {}", attendanceLogsId);
            AttendanceLogsResponse log = attendanceLogsService.checkOut(attendanceLogsId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Checked out successfully", log));
        } catch (RuntimeException ex) {
            logger.warn("Check out error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error checking out: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to check out", null));
        }
    }
}
