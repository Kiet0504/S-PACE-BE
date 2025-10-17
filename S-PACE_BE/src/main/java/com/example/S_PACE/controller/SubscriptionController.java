package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.SubscriptionPurchaseRequest;
import com.example.S_PACE.dto.response.PayOsResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.dto.response.SubscriptionPlanDTO;
import com.example.S_PACE.dto.response.UserSubscriptionDTO;
import com.example.S_PACE.service.SubscriptionService;
import com.example.S_PACE.utils.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@PreAuthorize("hasRole('EVENT_MANAGER')")
@Tag(name = "Subscription Management", description = "APIs for subscription and payment management")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanDTO>> listPlans() {
        List<SubscriptionPlanDTO> plans = subscriptionService.listPlans();
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/purchase")
    public ResponseEntity<UserSubscriptionDTO> purchasePlan(@RequestParam UUID userId, @RequestParam UUID planId) {
        try {
            UserSubscriptionDTO subscription = subscriptionService.purchasePlan(userId, planId);
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserSubscriptionDTO> getUserCurrentPlan(@PathVariable UUID userId) {
        try {
            UserSubscriptionDTO currentPlan = subscriptionService.getUserCurrentPlan(userId);
            return ResponseEntity.ok(currentPlan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/recruitment-limit")
    public ResponseEntity<Integer> getRecruitmentLimit(@PathVariable UUID userId) {
        try {
            int limit = subscriptionService.getRecruitmentLimitForUser(userId);
            return ResponseEntity.ok(limit);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/purchase-with-payment")
    @Operation(summary = "Purchase subscription plan with PayOS", description = "Create payment link for subscription plan purchase")
    public ResponseEntity<ResponseDTO<PayOsResponse>> purchaseSubscriptionWithPayment(
            @Valid @RequestBody SubscriptionPurchaseRequest request,
            HttpServletRequest httpRequest) {
        try {
            logger.info("Creating subscription payment for plan: {}", request.getPlanId());
            UUID userId = getUserIdFromToken(httpRequest);
            PayOsResponse paymentResponse = subscriptionService.createSubscriptionPayment(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(true, "Payment link created successfully", paymentResponse));
        } catch (IllegalArgumentException ex) {
            logger.warn("Subscription purchase validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error creating subscription payment: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Failed to create payment link", null));
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
