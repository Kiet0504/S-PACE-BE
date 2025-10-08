package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.PayOsRequest;
import com.example.S_PACE.dto.request.SubscriptionPurchaseRequest;
import com.example.S_PACE.dto.response.PayOsResponse;
import com.example.S_PACE.dto.response.SubscriptionPlanDTO;
import com.example.S_PACE.dto.response.UserSubscriptionDTO;
import com.example.S_PACE.pojo.SubscriptionPayment;
import com.example.S_PACE.pojo.SubscriptionPlan;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.pojo.UserSubscription;
import com.example.S_PACE.repository.SubscriptionPaymentRepository;
import com.example.S_PACE.repository.SubscriptionPlanRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.repository.UserSubscriptionRepository;
import com.example.S_PACE.service.PaymentService;
import com.example.S_PACE.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionPaymentRepository subscriptionPaymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanDTO> listPlans() {
        return subscriptionPlanRepository.findAll().stream()
                .sorted(Comparator.comparing(SubscriptionPlan::getMaxRecruitmentLimit))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserSubscriptionDTO purchasePlan(UUID userId, UUID planId) {
        if (userId == null || planId == null) {
            throw new IllegalArgumentException("UserId and PlanId are required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .build();
        userSubscription = userSubscriptionRepository.save(userSubscription);
        return toDto(userSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSubscriptionDTO getUserCurrentPlan(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
        return userSubscriptionRepository.findTopByUserIdOrderByPurchasedAtDesc(userId)
                .map(this::toDto)
                .orElseGet(() -> UserSubscriptionDTO.builder()
                        .userId(userId)
                        .planId(null)
                        .planName("Free")
                        .maxRecruitmentLimit(30)
                        .purchasedAt(null)
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public int getRecruitmentLimitForUser(UUID userId) {
        return getUserCurrentPlan(userId).getMaxRecruitmentLimit();
    }

    private SubscriptionPlanDTO toDto(SubscriptionPlan p) {
        return SubscriptionPlanDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .maxRecruitmentLimit(p.getMaxRecruitmentLimit())
                .build();
    }

    private UserSubscriptionDTO toDto(UserSubscription us) {
        return UserSubscriptionDTO.builder()
                .userId(us.getUser().getUserId())
                .planId(us.getPlan().getId())
                .planName(us.getPlan().getName())
                .maxRecruitmentLimit(us.getPlan().getMaxRecruitmentLimit())
                .purchasedAt(us.getPurchasedAt())
                .build();
    }

    @Override
    @Transactional
    public PayOsResponse createSubscriptionPayment(UUID userId, SubscriptionPurchaseRequest request) {
        if (userId == null || request.getPlanId() == null) {
            throw new IllegalArgumentException("UserId and PlanId are required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        // Check if user already has a pending payment for any plan
        subscriptionPaymentRepository.findByUserUserIdAndStatus(userId, SubscriptionPayment.PaymentStatus.PENDING)
                .ifPresent(payment -> {
                    throw new IllegalArgumentException("You already have a pending payment. Please complete or cancel it first.");
                });

        // Create PayOS payment request
        String description = request.getDescription() != null ? request.getDescription() :
                "Subscription upgrade to " + plan.getName() + " plan";
        String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() :
                "http://localhost:3000/subscription/success";
        String cancelUrl = request.getCancelUrl() != null ? request.getCancelUrl() :
                "http://localhost:3000/subscription/cancel";

        PayOsRequest payOsRequest = new PayOsRequest(
                plan.getName() + " Subscription Plan",
                description,
                plan.getPrice().intValue(),
                returnUrl,
                cancelUrl
        );

        PayOsResponse payOsResponse = paymentService.createPaymentLink(payOsRequest);

        // Create subscription payment record
        SubscriptionPayment subscriptionPayment = SubscriptionPayment.builder()
                .user(user)
                .plan(plan)
                .orderCode(payOsResponse.getOrderCode())
                .amount(plan.getPrice())
                .status(SubscriptionPayment.PaymentStatus.PENDING)
                .paymentUrl(payOsResponse.getCheckoutUrl())
                .build();

        subscriptionPaymentRepository.save(subscriptionPayment);

        return payOsResponse;
    }

    @Override
    @Transactional
    public void confirmSubscriptionPayment(Long orderCode) {
        SubscriptionPayment payment = subscriptionPaymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order code: " + orderCode));

        if (payment.getStatus() != SubscriptionPayment.PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Payment is not in pending status");
        }

        // Update payment status to PAID
        payment.setStatus(SubscriptionPayment.PaymentStatus.PAID);
        subscriptionPaymentRepository.save(payment);

        // Create user subscription
        UserSubscription userSubscription = UserSubscription.builder()
                .user(payment.getUser())
                .plan(payment.getPlan())
                .build();
        userSubscriptionRepository.save(userSubscription);
    }
}
