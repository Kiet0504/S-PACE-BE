package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.CreateRatingRequest;
import com.example.S_PACE.dto.request.UpdateRatingRequest;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.dto.response.RatingListResponse;
import com.example.S_PACE.dto.response.RatingResponse;
import com.example.S_PACE.dto.response.RatingSummaryResponse;
import com.example.S_PACE.service.RatingService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Rating Management", description = "Rating management APIs for collaborators")
public class RatingController {

    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);

    @Autowired
    private RatingService ratingService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "Create a new rating", description = "Create a rating for a collaborator in an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rating created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Rating already exists")
    })
    public ResponseEntity<ResponseDTO<RatingResponse>> createRating(
            @Valid @RequestBody CreateRatingRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID currentUserId = getCurrentUserId(httpRequest);
            RatingResponse rating = ratingService.createRating(request, currentUserId);
            
            ResponseDTO<RatingResponse> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Rating created successfully");
            response.setData(rating);
            
            logger.info("Rating created successfully for collaborator: {} by user: {}", 
                       request.getCollaboratorId(), currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating rating: {}", e.getMessage());
            ResponseDTO<RatingResponse> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to create rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{ratingId}")
    @Operation(summary = "Update a rating", description = "Update an existing rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Rating not found")
    })
    public ResponseEntity<ResponseDTO<RatingResponse>> updateRating(
            @PathVariable UUID ratingId,
            @Valid @RequestBody UpdateRatingRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID currentUserId = getCurrentUserId(httpRequest);
            RatingResponse rating = ratingService.updateRating(ratingId, request, currentUserId);
            
            ResponseDTO<RatingResponse> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Rating updated successfully");
            response.setData(rating);
            
            logger.info("Rating updated successfully: {} by user: {}", ratingId, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating rating: {}", e.getMessage());
            ResponseDTO<RatingResponse> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to update rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{ratingId}")
    @Operation(summary = "Delete a rating", description = "Delete an existing rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Rating not found")
    })
    public ResponseEntity<ResponseDTO<Void>> deleteRating(
            @PathVariable UUID ratingId,
            HttpServletRequest httpRequest) {
        try {
            UUID currentUserId = getCurrentUserId(httpRequest);
            ratingService.deleteRating(ratingId, currentUserId);
            
            ResponseDTO<Void> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Rating deleted successfully");
            
            logger.info("Rating deleted successfully: {} by user: {}", ratingId, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting rating: {}", e.getMessage());
            ResponseDTO<Void> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to delete rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{ratingId}")
    @Operation(summary = "Get rating by ID", description = "Retrieve a specific rating by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Rating not found")
    })
    public ResponseEntity<ResponseDTO<RatingResponse>> getRatingById(@PathVariable UUID ratingId) {
        try {
            RatingResponse rating = ratingService.getRatingById(ratingId);
            
            ResponseDTO<RatingResponse> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Rating retrieved successfully");
            response.setData(rating);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving rating: {}", e.getMessage());
            ResponseDTO<RatingResponse> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to retrieve rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/collaborator/{collaboratorId}")
    @Operation(summary = "Get ratings for a collaborator", description = "Get all ratings for a specific collaborator with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ratings retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<RatingListResponse>> getRatingsByCollaborator(
            @PathVariable UUID collaboratorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "ratingDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        try {
            Sort sort = sortOrder.equalsIgnoreCase("asc") ? 
                    Sort.by(Sort.Direction.ASC, sortBy) : 
                    Sort.by(Sort.Direction.DESC, sortBy);
            
            Pageable pageable = PageRequest.of(page - 1, limit, sort);
            RatingListResponse ratings = ratingService.getRatingsByCollaborator(collaboratorId, pageable);
            
            ResponseDTO<RatingListResponse> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Ratings retrieved successfully");
            response.setData(ratings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving ratings by collaborator: {}", e.getMessage());
            ResponseDTO<RatingListResponse> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to retrieve ratings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get ratings for an event", description = "Get all ratings for a specific event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event ratings retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<RatingListResponse>> getRatingsByEvent(@PathVariable UUID eventId) {
        try {
            RatingListResponse ratings = ratingService.getRatingsByEvent(eventId);
            
            ResponseDTO<RatingListResponse> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Event ratings retrieved successfully");
            response.setData(ratings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving ratings by event: {}", e.getMessage());
            ResponseDTO<RatingListResponse> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to retrieve event ratings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/event/{eventId}/collaborator/{collaboratorId}")
    @Operation(summary = "Get rating for specific event-collaborator", description = "Get rating for a specific event-collaborator combination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<RatingResponse>> getRatingByEventAndCollaborator(
            @PathVariable UUID eventId,
            @PathVariable UUID collaboratorId) {
        try {
            RatingResponse rating = ratingService.getRatingByEventAndCollaborator(eventId, collaboratorId);
            
            ResponseDTO<RatingResponse> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Rating retrieved successfully");
            response.setData(rating);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving rating by event and collaborator: {}", e.getMessage());
            ResponseDTO<RatingResponse> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to retrieve rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/collaborator/{collaboratorId}/summary")
    @Operation(summary = "Get rating summary for collaborator", description = "Get comprehensive rating summary for a collaborator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating summary retrieved successfully")
    })
    public ResponseEntity<ResponseDTO<RatingSummaryResponse>> getRatingSummary(@PathVariable UUID collaboratorId) {
        try {
            RatingSummaryResponse summary = ratingService.getRatingSummary(collaboratorId);
            
            ResponseDTO<RatingSummaryResponse> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Rating summary retrieved successfully");
            response.setData(summary);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving rating summary: {}", e.getMessage());
            ResponseDTO<RatingSummaryResponse> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to retrieve rating summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/can-rate/event/{eventId}/collaborator/{collaboratorId}")
    @Operation(summary = "Check if user can rate", description = "Check if current user can rate a collaborator for an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission check completed")
    })
    public ResponseEntity<ResponseDTO<Boolean>> canRate(
            @PathVariable UUID eventId,
            @PathVariable UUID collaboratorId,
            HttpServletRequest httpRequest) {
        try {
            UUID currentUserId = getCurrentUserId(httpRequest);
            boolean canRate = ratingService.canRate(eventId, collaboratorId, currentUserId);
            
            ResponseDTO<Boolean> response = new ResponseDTO<>();
            response.setSuccess(true);
            response.setMessage("Permission check completed");
            response.setData(canRate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking rating permission: {}", e.getMessage());
            ResponseDTO<Boolean> response = new ResponseDTO<>();
            response.setSuccess(false);
            response.setMessage("Failed to check permission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    private UUID getCurrentUserId(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token == null) {
            throw new RuntimeException("No authentication token found");
        }
        String userIdStr = jwtTokenProvider.getUserIdFromJWT(token);
        return UUID.fromString(userIdStr);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
