package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.CreateRatingRequest;
import com.example.S_PACE.dto.request.UpdateRatingRequest;
import com.example.S_PACE.dto.response.PaginationResponse;
import com.example.S_PACE.dto.response.RatingListResponse;
import com.example.S_PACE.dto.response.RatingResponse;
import com.example.S_PACE.dto.response.RatingSummaryResponse;
import com.example.S_PACE.enums.EventStatus;
import com.example.S_PACE.exception.AuthenticationException;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.Rating;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.repository.RatingRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Override
    public RatingResponse createRating(CreateRatingRequest request, UUID currentUserId) {
        // Validate business rules
        validateRatingCreation(request, currentUserId);

        // Check if rating already exists
        if (ratingRepository.existsByEventIdAndCollaboratorIdAndRatedBy(
                request.getEventId(), request.getCollaboratorId(), currentUserId)) {
            throw new IllegalArgumentException("Rating already exists for this event-collaborator combination");
        }

        // Create new rating
        Rating rating = new Rating();
        
        // Set entity relationships
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        rating.setEvent(event);
        
        User collaborator = userRepository.findById(request.getCollaboratorId())
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));
        rating.setCollaborator(collaborator);
        
        User ratedByUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        rating.setRatedBy(ratedByUser);
        
        // ratingScore is now calculated automatically by database trigger
        rating.setRatingComment(request.getRatingComment());
        rating.setPunctualityScore(request.getPunctualityScore() != null ? request.getPunctualityScore() : BigDecimal.ZERO);
        rating.setQualityScore(request.getQualityScore() != null ? request.getQualityScore() : BigDecimal.ZERO);
        rating.setAttitudeScore(request.getAttitudeScore() != null ? request.getAttitudeScore() : BigDecimal.ZERO);
        rating.setTeamworkScore(request.getTeamworkScore() != null ? request.getTeamworkScore() : BigDecimal.ZERO);

        Rating savedRating = ratingRepository.save(rating);
        return convertToRatingResponse(savedRating);
    }

    @Override
    public RatingResponse updateRating(UUID ratingId, UpdateRatingRequest request, UUID currentUserId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));

        // Check permissions
        if (!rating.getRatedBy().getUserId().equals(currentUserId) && !hasAdminPermission(currentUserId)) {
            throw new AuthenticationException("You don't have permission to update this rating");
        }

        // Update fields if provided
        // ratingScore is now calculated automatically by database trigger
        if (request.getRatingComment() != null) {
            rating.setRatingComment(request.getRatingComment());
        }
        if (request.getPunctualityScore() != null) {
            rating.setPunctualityScore(request.getPunctualityScore());
        }
        if (request.getQualityScore() != null) {
            rating.setQualityScore(request.getQualityScore());
        }
        if (request.getAttitudeScore() != null) {
            rating.setAttitudeScore(request.getAttitudeScore());
        }
        if (request.getTeamworkScore() != null) {
            rating.setTeamworkScore(request.getTeamworkScore());
        }

        rating.setUpdatedAt(LocalDateTime.now());
        Rating updatedRating = ratingRepository.save(rating);
        return convertToRatingResponse(updatedRating);
    }

    @Override
    public void deleteRating(UUID ratingId, UUID currentUserId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));

        // Check permissions
        if (!rating.getRatedBy().getUserId().equals(currentUserId) && !hasAdminPermission(currentUserId)) {
            throw new AuthenticationException("You don't have permission to delete this rating");
        }

        ratingRepository.delete(rating);
    }

    @Override
    public RatingResponse getRatingById(UUID ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));
        return convertToRatingResponse(rating);
    }

    @Override
    public RatingListResponse getRatingsByCollaborator(UUID collaboratorId, Pageable pageable) {
        Page<Rating> ratingPage = ratingRepository.findByCollaboratorIdOrderByRatingDateDesc(collaboratorId, pageable);
        
        List<RatingResponse> ratings = ratingPage.getContent().stream()
                .map(this::convertToRatingResponse)
                .collect(Collectors.toList());

        PaginationResponse pagination = new PaginationResponse(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                ratingPage.getTotalElements(),
                ratingPage.getTotalPages()
        );

        return new RatingListResponse(ratings, pagination);
    }

    @Override
    public RatingListResponse getRatingsByEvent(UUID eventId) {
        List<Rating> ratings = ratingRepository.findByEventIdOrderByRatingDateDesc(eventId);
        
        List<RatingResponse> ratingResponses = ratings.stream()
                .map(this::convertToRatingResponse)
                .collect(Collectors.toList());

        return new RatingListResponse(ratingResponses, null);
    }

    @Override
    public RatingResponse getRatingByEventAndCollaborator(UUID eventId, UUID collaboratorId) {
        Optional<Rating> rating = ratingRepository.findByEventIdAndCollaboratorId(eventId, collaboratorId);
        return rating.map(this::convertToRatingResponse).orElse(null);
    }

    @Override
    public RatingSummaryResponse getRatingSummary(UUID collaboratorId) {
        // Get collaborator info
        User collaborator = userRepository.findById(collaboratorId)
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

        // Get average rating and total count
        Double avgRating = ratingRepository.getAverageRatingByCollaboratorId(collaboratorId);
        Long totalRatings = ratingRepository.getTotalRatingsByCollaboratorId(collaboratorId);

        // Get rating distribution
        List<Object[]> distributionData = ratingRepository.getRatingDistributionByCollaboratorId(collaboratorId);
        Map<String, Integer> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(String.valueOf(i), 0);
        }
        for (Object[] data : distributionData) {
            String score = String.valueOf(data[0]);
            Integer count = ((Number) data[1]).intValue();
            ratingDistribution.put(score, count);
        }

        // Get average scores for different criteria
        Map<String, BigDecimal> averageScores = new HashMap<>();
        averageScores.put("punctuality", roundToTwoDecimalPlaces(
                BigDecimal.valueOf(ratingRepository.getAveragePunctualityScore(collaboratorId) != null ? 
                        ratingRepository.getAveragePunctualityScore(collaboratorId) : 0.0)));
        averageScores.put("quality", roundToTwoDecimalPlaces(
                BigDecimal.valueOf(ratingRepository.getAverageQualityScore(collaboratorId) != null ? 
                        ratingRepository.getAverageQualityScore(collaboratorId) : 0.0)));
        averageScores.put("attitude", roundToTwoDecimalPlaces(
                BigDecimal.valueOf(ratingRepository.getAverageAttitudeScore(collaboratorId) != null ? 
                        ratingRepository.getAverageAttitudeScore(collaboratorId) : 0.0)));
        averageScores.put("teamwork", roundToTwoDecimalPlaces(
                BigDecimal.valueOf(ratingRepository.getAverageTeamworkScore(collaboratorId) != null ? 
                        ratingRepository.getAverageTeamworkScore(collaboratorId) : 0.0)));

        // Get recent ratings (last 5)
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 5);
        List<Rating> recentRatings = ratingRepository.getRecentRatingsByCollaboratorId(collaboratorId, pageable);
        List<RatingResponse> recentRatingResponses = recentRatings.stream()
                .map(this::convertToRatingResponse)
                .collect(Collectors.toList());

        return new RatingSummaryResponse(
                collaboratorId,
                collaborator.getFullName(),
                roundToTwoDecimalPlaces(BigDecimal.valueOf(avgRating != null ? avgRating : 0.0)),
                totalRatings.intValue(),
                ratingDistribution,
                averageScores,
                recentRatingResponses
        );
    }

    @Override
    public boolean canRate(UUID eventId, UUID collaboratorId, UUID currentUserId) {
        // 1. Check if user is event creator
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getCreatedBy().getUserId().equals(currentUserId)) {
            return false;
        }

        // 2. Check if event status is COMPLETED
        if (!EventStatus.COMPLETED.equals(event.getStatus())) {
            return false;
        }

        // 3. Check if rating already exists
        boolean alreadyRated = ratingRepository.existsByEventIdAndCollaboratorIdAndRatedBy(
                eventId, collaboratorId, currentUserId);

        return !alreadyRated;
    }

    private void validateRatingCreation(CreateRatingRequest request, UUID currentUserId) {
        // Check if event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if collaborator exists
        userRepository.findById(request.getCollaboratorId())
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

        // Check if current user is event creator
        if (!event.getCreatedBy().getUserId().equals(currentUserId)) {
            throw new AuthenticationException("Only event creators can rate collaborators");
        }

        // Check if event status is COMPLETED
        if (!EventStatus.COMPLETED.equals(event.getStatus())) {
            throw new IllegalArgumentException("Can only rate for completed events");
        }

        // Check if user has permission to rate
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!hasEventManagerPermission(currentUser) && !hasAdminPermission(currentUserId)) {
            throw new AuthenticationException("Insufficient permissions to create rating");
        }
    }

    private boolean hasAdminPermission(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getRole().getRoleName().equals("ADMIN") ||
               user.getRole().getRoleName().equals("COMPANY_ADMIN");
    }

    private boolean hasEventManagerPermission(User user) {
        return user.getRole().getRoleName().equals("EVENT_MANAGER");
    }

    private RatingResponse convertToRatingResponse(Rating rating) {
        // Get event title
        String eventTitle = rating.getEvent() != null ? rating.getEvent().getTitle() : null;

        // Get collaborator info
        String collaboratorName = rating.getCollaborator() != null ? rating.getCollaborator().getFullName() : null;
        String collaboratorEmail = rating.getCollaborator() != null ? rating.getCollaborator().getEmail() : null;

        // Get rater info
        String raterName = rating.getRatedBy() != null ? rating.getRatedBy().getFullName() : null;

        return new RatingResponse(
                rating.getRatingId(),
                rating.getEvent() != null ? rating.getEvent().getEventId() : null,
                eventTitle,
                rating.getCollaborator() != null ? rating.getCollaborator().getUserId() : null,
                collaboratorName,
                collaboratorEmail,
                rating.getRatedBy() != null ? rating.getRatedBy().getUserId() : null,
                raterName,
                rating.getRatingScore(),
                rating.getRatingComment(),
                rating.getRatingDate(),
                rating.getUpdatedAt(),
                rating.getPunctualityScore(),
                rating.getQualityScore(),
                rating.getAttitudeScore(),
                rating.getTeamworkScore()
        );
    }

    private BigDecimal roundToTwoDecimalPlaces(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
