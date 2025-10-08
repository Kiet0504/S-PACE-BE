package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.EventTasksRequest;
import com.example.S_PACE.dto.response.EventTasksResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.enums.EventTasksStatus;
import com.example.S_PACE.service.EventTasksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/event-tasks")
@Tag(name = "Event Tasks Management", description = "Event tasks management APIs")
public class EventTasksController {

    private static final Logger logger = LoggerFactory.getLogger(EventTasksController.class);

    @Autowired
    private EventTasksService eventTasksService;

    @PostMapping
    @Operation(summary = "Create a new event task", description = "Create a new event task for a team")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Event task created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Team or User not found")
    })
    public ResponseEntity<ResponseDTO<EventTasksResponse>> createEventTask(@RequestBody EventTasksRequest request) {
        try {
            logger.info("Creating event task request received for: {}", request.getTitle());
            EventTasksResponse response = eventTasksService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Event task created successfully", response));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event task creation validation error: {}", ex.getMessage());
            HttpStatus status = ex.getMessage().contains("not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Event task creation error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while creating the event task", null));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event task by ID", description = "Retrieve an event task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event task retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Event task not found")
    })
    public ResponseEntity<ResponseDTO<EventTasksResponse>> getEventTaskById(@PathVariable UUID id) {
        try {
            logger.info("Getting event task by ID: {}", id);
            EventTasksResponse response = eventTasksService.getById(id);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event task retrieved successfully", response));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event task not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error retrieving event task: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while retrieving the event task", null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all event tasks", description = "Retrieve all event tasks")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event tasks retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<List<EventTasksResponse>>> getAllEventTasks() {
        try {
            logger.info("Getting all event tasks");
            List<EventTasksResponse> response = eventTasksService.getAll();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event tasks retrieved successfully", response));
        } catch (Exception ex) {
            logger.error("Error retrieving event tasks: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while retrieving event tasks", null));
        }
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get event tasks by team", description = "Retrieve all event tasks for a specific team")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event tasks retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<List<EventTasksResponse>>> getEventTasksByTeam(@PathVariable UUID teamId) {
        try {
            logger.info("Getting event tasks for team ID: {}", teamId);
            List<EventTasksResponse> response = eventTasksService.getByTeamId(teamId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event tasks retrieved successfully", response));
        } catch (Exception ex) {
            logger.error("Error retrieving event tasks for team: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while retrieving event tasks", null));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get event tasks by assigned user", description = "Retrieve all event tasks assigned to a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event tasks retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<List<EventTasksResponse>>> getEventTasksByUser(@PathVariable UUID userId) {
        try {
            logger.info("Getting event tasks for user ID: {}", userId);
            List<EventTasksResponse> response = eventTasksService.getByAssignedUser(userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event tasks retrieved successfully", response));
        } catch (Exception ex) {
            logger.error("Error retrieving event tasks for user: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while retrieving event tasks", null));
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get event tasks by status", description = "Retrieve all event tasks with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event tasks retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<List<EventTasksResponse>>> getEventTasksByStatus(@PathVariable EventTasksStatus status) {
        try {
            logger.info("Getting event tasks with status: {}", status);
            List<EventTasksResponse> response = eventTasksService.getByStatus(status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event tasks retrieved successfully", response));
        } catch (Exception ex) {
            logger.error("Error retrieving event tasks by status: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while retrieving event tasks", null));
        }
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get event tasks by event", description = "Retrieve all event tasks for a specific event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event tasks retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<List<EventTasksResponse>>> getEventTasksByEvent(@PathVariable UUID eventId) {
        try {
            logger.info("Getting event tasks for event ID: {}", eventId);
            List<EventTasksResponse> response = eventTasksService.getByEventId(eventId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event tasks retrieved successfully", response));
        } catch (Exception ex) {
            logger.error("Error retrieving event tasks for event: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while retrieving event tasks", null));
        }
    }

    @GetMapping("/deadline")
    @Operation(summary = "Get event tasks by deadline range", description = "Retrieve all event tasks within a deadline range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event tasks retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<List<EventTasksResponse>>> getEventTasksByDeadlineRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            logger.info("Getting event tasks with deadline between {} and {}", startDate, endDate);
            List<EventTasksResponse> response = eventTasksService.getByDeadlineBetween(startDate, endDate);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event tasks retrieved successfully", response));
        } catch (Exception ex) {
            logger.error("Error retrieving event tasks by deadline range: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while retrieving event tasks", null));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event task", description = "Update an existing event task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event task updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Event task not found")
    })
    public ResponseEntity<ResponseDTO<EventTasksResponse>> updateEventTask(
            @PathVariable UUID id, @RequestBody EventTasksRequest request) {
        try {
            logger.info("Updating event task with ID: {}", id);
            EventTasksResponse response = eventTasksService.update(id, request);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event task updated successfully", response));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event task update validation error: {}", ex.getMessage());
            HttpStatus status = ex.getMessage().contains("not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Event task update error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while updating the event task", null));
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update event task status", description = "Update the status of an existing event task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event task status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Event task not found")
    })
    public ResponseEntity<ResponseDTO<EventTasksResponse>> updateEventTaskStatus(
            @PathVariable UUID id, @RequestParam EventTasksStatus status) {
        try {
            logger.info("Updating status of event task with ID: {} to {}", id, status);
            EventTasksResponse response = eventTasksService.updateStatus(id, status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event task status updated successfully", response));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event task status update error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Event task status update error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while updating the event task status", null));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event task", description = "Delete an existing event task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Event task not found")
    })
    public ResponseEntity<ResponseDTO<Void>> deleteEventTask(@PathVariable UUID id) {
        try {
            logger.info("Deleting event task with ID: {}", id);
            eventTasksService.delete(id);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Event task deleted successfully", null));
        } catch (IllegalArgumentException ex) {
            logger.warn("Event task deletion error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Event task deletion error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "An error occurred while deleting the event task", null));
        }
    }
}