package com.example.S_PACE.controller;

import com.example.S_PACE.dto.response.WebhookResponse;
import com.example.S_PACE.service.PaymentService;
import com.example.S_PACE.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Webhooks", description = "Webhook endpoints for PayOS payment notifications")
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    @GetMapping("/payos-payment")
    @Operation(summary = "Test PayOS Webhook Endpoint", description = "Test endpoint for webhook connectivity")
    public ResponseEntity<String> testWebhookEndpoint() {
        return ResponseEntity.ok("PayOS webhook endpoint is working! Ready to receive payments.");
    }

    @GetMapping("")
    @Operation(summary = "Webhook Root Endpoint", description = "Root webhook endpoint for PayOS verification")
    public ResponseEntity<String> webhookRoot() {
        return ResponseEntity.ok("Webhook service is running");
    }

    @PostMapping("/payos-payment")
    @Operation(summary = "PayOS Payment Webhook", description = "Webhook endpoint to receive payment notifications from PayOS")
    public ResponseEntity<Map<String, Object>> handlePayOSWebhook(
            @RequestBody(required = false) String webhookBody,
            @RequestHeader(value = "PayOS-Signature", required = false) String signature,
            @RequestHeader(value = "ngrok-skip-browser-warning", required = false) String skipWarning) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("=== PayOS Webhook Received ===");
            log.info("Signature: {}", signature);
            log.info("Body: {}", webhookBody);

            // Handle empty body or verification request
            if (webhookBody == null || webhookBody.trim().isEmpty()) {
                log.info("Empty webhook body - treating as verification request");
                response.put("success", true);
                response.put("message", "Webhook endpoint verified");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(response);
            }

            // Parse webhook body to check if it's a real payment notification
            try {
                // Try to parse and extract payment data even without signature
                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(webhookBody);

                // Check if this is a successful payment webhook
                if (jsonNode.has("data") && jsonNode.has("code")) {
                    String code = jsonNode.get("code").asText();

                    if ("00".equals(code)) { // PayOS success code
                        // Extract order information
                        com.fasterxml.jackson.databind.JsonNode dataNode = jsonNode.get("data");
                        long orderCode = dataNode.get("orderCode").asLong();
                        int amount = dataNode.get("amount").asInt();

                        log.info("Payment successful - OrderCode: {}, Amount: {}", orderCode, amount);

                        // Process the payment
                        try {
                            subscriptionService.confirmSubscriptionPayment(orderCode);
                            log.info("Successfully confirmed subscription payment for orderCode: {}", orderCode);

                            response.put("success", true);
                            response.put("message", "Payment processed successfully");
                            response.put("orderCode", orderCode);
                            response.put("timestamp", System.currentTimeMillis());
                            return ResponseEntity.ok(response);

                        } catch (Exception e) {
                            log.error("Error confirming subscription payment: {}", e.getMessage(), e);
                            // Still return success to PayOS to prevent retries
                            response.put("success", true);
                            response.put("message", "Webhook received but payment processing failed: " + e.getMessage());
                            response.put("orderCode", orderCode);
                            response.put("timestamp", System.currentTimeMillis());
                            return ResponseEntity.ok(response);
                        }
                    }
                }
            } catch (Exception parseException) {
                log.warn("Could not parse webhook body as payment notification: {}", parseException.getMessage());
            }

            // If signature exists, try to verify with PayOS SDK
            if (signature != null && !signature.isEmpty()) {
                log.info("Processing webhook with signature verification");
                WebhookResponse webhookResponse = paymentService.verifyPaymentWebhook(webhookBody);

                log.info("Successfully verified payment webhook for orderCode: {}, status: {}",
                        webhookResponse.getOrderCode(), webhookResponse.getCode());

                handleSuccessfulPayment(webhookResponse);

                response.put("success", true);
                response.put("message", "Webhook processed successfully");
                response.put("orderCode", webhookResponse.getOrderCode());
                response.put("timestamp", System.currentTimeMillis());

                return ResponseEntity.ok(response);
            }

            // Default success response for webhook validation
            response.put("success", true);
            response.put("message", "Webhook endpoint is active");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing PayOS webhook: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error processing webhook: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.OK).body(response); // Return 200 even on error to prevent PayOS retries
        }
    }

    private void handleSuccessfulPayment(WebhookResponse webhookResponse) {
        try {
            log.info("Processing successful payment for order: {}", webhookResponse.getOrderCode());

            // Check if this is a subscription payment and handle accordingly
            try {
                subscriptionService.confirmSubscriptionPayment(webhookResponse.getOrderCode());
                log.info("Successfully confirmed subscription payment for orderCode: {}", webhookResponse.getOrderCode());
            } catch (IllegalArgumentException e) {
                // This might not be a subscription payment, or it's already been processed
                log.info("Payment orderCode {} is not a subscription payment or already processed: {}",
                        webhookResponse.getOrderCode(), e.getMessage());

                // Handle other types of payments here
                // Example: Regular product purchases, donations, etc.
            }

            log.info("Successfully handled payment for orderCode: {} with amount: {}",
                    webhookResponse.getOrderCode(), webhookResponse.getAmount());

        } catch (Exception e) {
            log.error("Error handling successful payment for orderCode {}: {}",
                    webhookResponse.getOrderCode(), e.getMessage(), e);
        }
    }
}