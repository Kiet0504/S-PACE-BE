package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.SubscriptionPurchaseRequest;
import com.example.S_PACE.dto.response.PayOsResponse;
import com.example.S_PACE.dto.response.SubscriptionPlanDTO;
import com.example.S_PACE.dto.response.UserSubscriptionDTO;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    List<SubscriptionPlanDTO> listPlans();
    UserSubscriptionDTO purchasePlan(UUID userId, UUID planId);
    UserSubscriptionDTO getUserCurrentPlan(UUID userId);
    int getRecruitmentLimitForUser(UUID userId);

    // Payment integration methods
    PayOsResponse createSubscriptionPayment(UUID userId, SubscriptionPurchaseRequest request);
    void confirmSubscriptionPayment(Long orderCode);
}

