package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.EventRegisterRequest;
import com.example.S_PACE.dto.response.EventRegistrationResponse;
import com.example.S_PACE.enums.EventRegistrationStatus;
import com.example.S_PACE.exception.AuthenticationException;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.EventRegistration;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.EventRegistrationRepository;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.repository.RatingRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.service.EmailService;
import com.example.S_PACE.service.EventRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(EventRegistrationServiceImpl.class);

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RatingRepository ratingRepository;

    private static final Integer DEFAULT_MIN_TOTAL_RATINGS = 5; // Số lượng rating tối thiểu

    @Override
    public EventRegistrationResponse registerForEvent(EventRegisterRequest request, UUID userId) {
        logger.info("Processing event registration for user: {} and event: {}", userId, request.getEventId());

        // Validate user exists and is a collaborator
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"COLLABORATOR".equals(user.getRole().getRoleName())) {
            throw new AuthenticationException("Only collaborators can register for events");
        }

        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if user already registered for this event
        eventRegistrationRepository.findByUserIdAndEventId(userId, request.getEventId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User already registered for this event");
                });

        // Create new registration
        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .fullName(request.getFullName())
                .gender(request.getGender())
                .profession(request.getProfession())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .reasonForParticipation(request.getReasonForParticipation())
                .filePath(request.getFilePath())
                .birthYear(request.getBirthYear())
                .status(EventRegistrationStatus.PENDING)
                .build();

        registration = eventRegistrationRepository.save(registration);
        logger.info("Event registration created successfully with ID: {}", registration.getEventRegistrationId());

        // Tự động kiểm tra và duyệt nếu CTV có rating đạt ngưỡng (với tiêu chí mặc định)
        try {
            // Sử dụng tiêu chí mặc định: tổng điểm ≥ 16 (4 điểm mỗi cột x 4 cột)
            autoApproveByRating(registration.getEventRegistrationId(), 
                                4.0,  // minPunctuality
                                4.0,  // minQuality
                                4.0,  // minAttitude
                                4.0,  // minTeamwork
                                16.0, // minTotalScore (4 x 4)
                                DEFAULT_MIN_TOTAL_RATINGS);
        } catch (Exception e) {
            logger.warn("Failed to auto-approve registration {} during creation: {}", 
                registration.getEventRegistrationId(), e.getMessage());
            // Không throw exception, chỉ log warning để không ảnh hưởng đến flow chính
        }

        return mapToResponse(registration);
    }

    @Override
    public EventRegistrationResponse updateRegistrationStatus(UUID registrationId, EventRegistrationStatus status,
                                                            String reviewNotes, UUID reviewerId) {
        logger.info("Updating registration status for ID: {} to status: {}", registrationId, status);

        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

        // Store old status to check if it changed
        EventRegistrationStatus oldStatus = registration.getStatus();

        registration.setStatus(status);
        registration.setReviewNotes(reviewNotes);
        registration.setReviewedBy(reviewer);
        registration.setReviewedAt(LocalDateTime.now());

        registration = eventRegistrationRepository.save(registration);
        logger.info("Registration status updated successfully");

        // Send email notification if status changed from PENDING to APPROVED
        if (oldStatus == EventRegistrationStatus.PENDING && status == EventRegistrationStatus.APPROVED) {
            try {
                logger.info("Sending approval email for registration: {}", registrationId);
                emailService.sendRegistrationApprovedEmail(registration);
            } catch (Exception e) {
                logger.error("Failed to send approval email for registration: {}. Error: {}",
                    registrationId, e.getMessage(), e);
                // Continue execution even if email fails
            }
        }

        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getAllRegistrations() {
        logger.info("Fetching all event registrations");
        return eventRegistrationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrationsByUserId(UUID userId) {
        logger.info("Fetching registrations for user: {}", userId);
        return eventRegistrationRepository.findByUserUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrationsByEventId(UUID eventId) {
        logger.info("Fetching registrations for event: {}", eventId);
        return eventRegistrationRepository.findByEventEventId(eventId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventRegistrationResponse getRegistrationById(UUID registrationId) {
        logger.info("Fetching registration by ID: {}", registrationId);
        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        
        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrationsByStatus(EventRegistrationStatus status) {
        logger.info("Fetching registrations with status: {}", status);
        return eventRegistrationRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventRegistrationResponse cancelRegistration(UUID registrationId, UUID userId) {
        logger.info("Cancelling registration: {} for user: {}", registrationId, userId);

        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Verify the user owns this registration
        if (!registration.getUser().getUserId().equals(userId)) {
            throw new AuthenticationException("You can only cancel your own registrations");
        }

        // Only allow cancellation of pending or approved registrations
        if (registration.getStatus() != EventRegistrationStatus.PENDING && 
            registration.getStatus() != EventRegistrationStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot cancel registration with status: " + registration.getStatus());
        }

        registration.setStatus(EventRegistrationStatus.CANCELLED);
        registration = eventRegistrationRepository.save(registration);

        logger.info("Registration cancelled successfully");
        return mapToResponse(registration);
    }

    private EventRegistrationResponse mapToResponse(EventRegistration registration) {
        return EventRegistrationResponse.builder()
                .eventRegistrationId(registration.getEventRegistrationId())
                .eventId(registration.getEvent().getEventId())
                .eventTitle(registration.getEvent().getTitle())
                .eventPicture(registration.getEvent().getPicture())
                .userId(registration.getUser().getUserId())
                .userEmail(registration.getUser().getEmail())
                .registrationDate(registration.getRegistrationDate())
                .reviewedBy(registration.getReviewedBy() != null ? registration.getReviewedBy().getUserId() : null)
                .reviewedByName(registration.getReviewedBy() != null ? registration.getReviewedBy().getFullName() : null)
                .reviewedAt(registration.getReviewedAt())
                .reviewNotes(registration.getReviewNotes())
                .status(registration.getStatus())
                .updatedAt(registration.getUpdatedAt())
                .fullName(registration.getFullName())
                .gender(registration.getGender())
                .profession(registration.getProfession())
                .phoneNumber(registration.getPhoneNumber())
                .address(registration.getAddress())
                .reasonForParticipation(registration.getReasonForParticipation())
                .filePath(registration.getFilePath())
                .birthYear(registration.getBirthYear())
                .userAvatar(registration.getUser().getAvatar()) // Add user's avatar
                .build();
    }

    @Override
    @Transactional
    public EventRegistrationResponse autoApproveByRating(UUID registrationId, 
                                                         Double minPunctuality, 
                                                         Double minQuality, 
                                                         Double minAttitude, 
                                                         Double minTeamwork, 
                                                         Double minTotalScore, 
                                                         Integer minTotalRatings) {
        logger.info("Attempting to auto-approve registration {} based on rating criteria chosen by BTC", registrationId);

        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Chỉ xử lý các đăng ký đang PENDING
        if (registration.getStatus() != EventRegistrationStatus.PENDING) {
            logger.debug("Registration {} is not PENDING (current status: {}), skipping auto-approval", 
                registrationId, registration.getStatus());
            return null;
        }

        UUID collaboratorId = registration.getUser().getUserId();

        // Kiểm tra xem BTC đã chọn ít nhất một tiêu chí rating chưa (ngoài minTotalRatings)
        boolean hasRatingCriteria = minPunctuality != null || minQuality != null || 
                                   minAttitude != null || minTeamwork != null || minTotalScore != null;
        
        if (!hasRatingCriteria) {
            logger.warn("BTC chưa chọn tiêu chí rating nào cho đăng ký {}. Chỉ kiểm tra số lượng rating.", registrationId);
            // Vẫn tiếp tục xử lý nhưng chỉ kiểm tra số lượng rating
        } else {
            logger.info("BTC đã chọn các tiêu chí rating - Punctuality: {}, Quality: {}, Attitude: {}, Teamwork: {}, TotalScore: {}",
                minPunctuality, minQuality, minAttitude, minTeamwork, minTotalScore);
        }

        // Set default cho minTotalRatings
        if (minTotalRatings == null) {
            minTotalRatings = DEFAULT_MIN_TOTAL_RATINGS;
        }

        // Lấy số lượng rating
        Long totalRatings = ratingRepository.getTotalRatingsByCollaboratorId(collaboratorId);
        
        // Kiểm tra số lượng rating tối thiểu
        if (totalRatings == null || totalRatings < minTotalRatings) {
            logger.debug("Collaborator {} has insufficient ratings (total: {}, required: {}), not auto-approving",
                collaboratorId, totalRatings, minTotalRatings);
            return null;
        }

        // Lấy average score cho từng cột
        Double avgPunctuality = ratingRepository.getAveragePunctualityScore(collaboratorId);
        Double avgQuality = ratingRepository.getAverageQualityScore(collaboratorId);
        Double avgAttitude = ratingRepository.getAverageAttitudeScore(collaboratorId);
        Double avgTeamwork = ratingRepository.getAverageTeamworkScore(collaboratorId);

        // Kiểm tra từng cột nếu có yêu cầu
        if (minPunctuality != null) {
            if (avgPunctuality == null || avgPunctuality < minPunctuality) {
                logger.debug("Collaborator {} punctuality score ({}) below threshold ({}), not auto-approving",
                    collaboratorId, avgPunctuality, minPunctuality);
                return null;
            }
        }

        if (minQuality != null) {
            if (avgQuality == null || avgQuality < minQuality) {
                logger.debug("Collaborator {} quality score ({}) below threshold ({}), not auto-approving",
                    collaboratorId, avgQuality, minQuality);
                return null;
            }
        }

        if (minAttitude != null) {
            if (avgAttitude == null || avgAttitude < minAttitude) {
                logger.debug("Collaborator {} attitude score ({}) below threshold ({}), not auto-approving",
                    collaboratorId, avgAttitude, minAttitude);
                return null;
            }
        }

        if (minTeamwork != null) {
            if (avgTeamwork == null || avgTeamwork < minTeamwork) {
                logger.debug("Collaborator {} teamwork score ({}) below threshold ({}), not auto-approving",
                    collaboratorId, avgTeamwork, minTeamwork);
                return null;
            }
        }

        // Tính tổng điểm (tổng 4 cột)
        Double totalScore = 0.0;
        if (avgPunctuality != null) totalScore += avgPunctuality;
        if (avgQuality != null) totalScore += avgQuality;
        if (avgAttitude != null) totalScore += avgAttitude;
        if (avgTeamwork != null) totalScore += avgTeamwork;

        // Kiểm tra tổng điểm nếu có yêu cầu
        if (minTotalScore != null && totalScore < minTotalScore) {
            logger.debug("Collaborator {} total score ({}) below threshold ({}), not auto-approving",
                collaboratorId, totalScore, minTotalScore);
            return null;
        }

        // Đạt tất cả điều kiện mà BTC đã chọn, tự động duyệt
        logger.info("CTV đáp ứng tất cả tiêu chí mà BTC đã chọn. Đang tự động duyệt đăng ký {} cho CTV {} với điểm số - " +
            "Đúng giờ: {}, Chất lượng: {}, Thái độ: {}, Làm việc nhóm: {}, Tổng điểm: {} ({} tổng số đánh giá)",
            registrationId, collaboratorId, avgPunctuality, avgQuality, avgAttitude, avgTeamwork, totalScore, totalRatings);

        // Tạo review notes chi tiết về các tiêu chí mà BTC đã chọn
        StringBuilder reviewNotes = new StringBuilder("Tự động duyệt dựa trên các tiêu chí rating mà BTC đã chọn:\n");
        reviewNotes.append(String.format("- Đúng giờ: %.2f", avgPunctuality != null ? avgPunctuality : 0.0));
        if (minPunctuality != null) {
            reviewNotes.append(String.format(" (BTC yêu cầu: ≥%.2f)", minPunctuality));
        }
        reviewNotes.append(String.format("\n- Chất lượng: %.2f", avgQuality != null ? avgQuality : 0.0));
        if (minQuality != null) {
            reviewNotes.append(String.format(" (BTC yêu cầu: ≥%.2f)", minQuality));
        }
        reviewNotes.append(String.format("\n- Thái độ: %.2f", avgAttitude != null ? avgAttitude : 0.0));
        if (minAttitude != null) {
            reviewNotes.append(String.format(" (BTC yêu cầu: ≥%.2f)", minAttitude));
        }
        reviewNotes.append(String.format("\n- Làm việc nhóm: %.2f", avgTeamwork != null ? avgTeamwork : 0.0));
        if (minTeamwork != null) {
            reviewNotes.append(String.format(" (BTC yêu cầu: ≥%.2f)", minTeamwork));
        }
        reviewNotes.append(String.format("\n- Tổng điểm: %.2f", totalScore));
        if (minTotalScore != null) {
            reviewNotes.append(String.format(" (BTC yêu cầu: ≥%.2f)", minTotalScore));
        }
        reviewNotes.append(String.format("\n- Tổng số đánh giá: %d", totalRatings));

        registration.setStatus(EventRegistrationStatus.APPROVED);
        registration.setReviewNotes(reviewNotes.toString());
        registration.setReviewedAt(LocalDateTime.now());
        // Không set reviewedBy vì đây là tự động

        registration = eventRegistrationRepository.save(registration);

        // Gửi email thông báo
        try {
            logger.info("Sending approval email for auto-approved registration: {}", registrationId);
            emailService.sendRegistrationApprovedEmail(registration);
        } catch (Exception e) {
            logger.error("Failed to send approval email for registration: {}. Error: {}",
                registrationId, e.getMessage(), e);
            // Continue execution even if email fails
        }

        logger.info("Registration {} auto-approved successfully", registrationId);
        return mapToResponse(registration);
    }

    @Override
    @Transactional
    public int autoApproveAllByRating(Double minPunctuality, 
                                      Double minQuality, 
                                      Double minAttitude, 
                                      Double minTeamwork, 
                                      Double minTotalScore, 
                                      Integer minTotalRatings) {
        if (minTotalRatings == null) {
            minTotalRatings = DEFAULT_MIN_TOTAL_RATINGS;
        }

        // Kiểm tra xem BTC đã chọn tiêu chí rating nào chưa
        boolean hasRatingCriteria = minPunctuality != null || minQuality != null || 
                                   minAttitude != null || minTeamwork != null || minTotalScore != null;

        logger.info("BTC bắt đầu quá trình duyệt nhanh tất cả đăng ký PENDING với các tiêu chí đã chọn - " +
            "Đúng giờ: {}, Chất lượng: {}, Thái độ: {}, Làm việc nhóm: {}, Tổng điểm: {}, Số lượng rating tối thiểu: {}",
            minPunctuality, minQuality, minAttitude, minTeamwork, minTotalScore, minTotalRatings);
        
        if (!hasRatingCriteria) {
            logger.warn("Lưu ý: BTC chưa chọn tiêu chí rating nào, sẽ chỉ kiểm tra số lượng rating cho tất cả đăng ký");
        }

        // Lấy tất cả các đăng ký đang PENDING
        List<EventRegistration> pendingRegistrations = eventRegistrationRepository
                .findByStatus(EventRegistrationStatus.PENDING);

        int approvedCount = 0;

        for (EventRegistration registration : pendingRegistrations) {
            try {
                // Sử dụng lại logic của autoApproveByRating để kiểm tra từng đăng ký
                EventRegistrationResponse result = autoApproveByRating(
                    registration.getEventRegistrationId(),
                    minPunctuality,
                    minQuality,
                    minAttitude,
                    minTeamwork,
                    minTotalScore,
                    minTotalRatings
                );

                if (result != null) {
                    approvedCount++;
                }
            } catch (Exception e) {
                logger.error("Error processing registration {} for auto-approval: {}",
                    registration.getEventRegistrationId(), e.getMessage(), e);
                // Continue with next registration
            }
        }

        logger.info("Hoàn thành quá trình duyệt nhanh. BTC đã chọn tiêu chí và hệ thống đã duyệt {} trên tổng số {} đăng ký đang PENDING",
            approvedCount, pendingRegistrations.size());

        return approvedCount;
    }
}
