package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.EventRegisterRequest;
import com.example.S_PACE.dto.response.EventRegistrationResponse;
import com.example.S_PACE.enums.EventRegistrationStatus;

import java.util.List;
import java.util.UUID;

public interface EventRegistrationService {
    
    EventRegistrationResponse registerForEvent(EventRegisterRequest request, UUID userId);
    
    EventRegistrationResponse updateRegistrationStatus(UUID registrationId, EventRegistrationStatus status, String reviewNotes, UUID reviewerId);
    
    List<EventRegistrationResponse> getAllRegistrations();
    
    List<EventRegistrationResponse> getRegistrationsByUserId(UUID userId);
    
    List<EventRegistrationResponse> getRegistrationsByEventId(UUID eventId);
    
    EventRegistrationResponse getRegistrationById(UUID registrationId);
    
    List<EventRegistrationResponse> getRegistrationsByStatus(EventRegistrationStatus status);
    
    EventRegistrationResponse cancelRegistration(UUID registrationId, UUID userId);
    
    /**
     * Tự động duyệt CTV dựa trên rating theo 4 cột
     * @param registrationId ID của đăng ký cần kiểm tra và duyệt
     * @param minPunctuality Điểm tối thiểu cho cột Đúng giờ (null = không kiểm tra)
     * @param minQuality Điểm tối thiểu cho cột Chất lượng (null = không kiểm tra)
     * @param minAttitude Điểm tối thiểu cho cột Thái độ (null = không kiểm tra)
     * @param minTeamwork Điểm tối thiểu cho cột Làm việc nhóm (null = không kiểm tra)
     * @param minTotalScore Tổng điểm tối thiểu (tổng 4 cột, null = không kiểm tra)
     * @param minTotalRatings Số lượng rating tối thiểu (mặc định 5)
     * @return EventRegistrationResponse nếu được duyệt tự động, null nếu không đủ điều kiện
     */
    EventRegistrationResponse autoApproveByRating(UUID registrationId, 
                                                  Double minPunctuality, 
                                                  Double minQuality, 
                                                  Double minAttitude, 
                                                  Double minTeamwork, 
                                                  Double minTotalScore, 
                                                  Integer minTotalRatings);
    
    /**
     * Tự động duyệt tất cả các đăng ký PENDING có rating đạt ngưỡng
     * @param minPunctuality Điểm tối thiểu cho cột Đúng giờ (null = không kiểm tra)
     * @param minQuality Điểm tối thiểu cho cột Chất lượng (null = không kiểm tra)
     * @param minAttitude Điểm tối thiểu cho cột Thái độ (null = không kiểm tra)
     * @param minTeamwork Điểm tối thiểu cho cột Làm việc nhóm (null = không kiểm tra)
     * @param minTotalScore Tổng điểm tối thiểu (tổng 4 cột, null = không kiểm tra)
     * @param minTotalRatings Số lượng rating tối thiểu (mặc định 5)
     * @return Số lượng đăng ký đã được tự động duyệt
     */
    int autoApproveAllByRating(Double minPunctuality, 
                               Double minQuality, 
                               Double minAttitude, 
                               Double minTeamwork, 
                               Double minTotalScore, 
                               Integer minTotalRatings);
}


