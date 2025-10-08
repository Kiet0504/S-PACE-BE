package com.example.S_PACE.controller;

import com.example.S_PACE.dto.response.WebhookResponse;
import com.example.S_PACE.service.PaymentService;
import com.example.S_PACE.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> handlePayOSWebhook(
            @RequestBody String webhookBody,
            @RequestHeader(value = "PayOS-Signature", required = false) String signature,
            @RequestHeader(value = "ngrok-skip-browser-warning", required = false) String skipWarning) {
        try {
            log.info("Received PayOS webhook - Body: {}, Signature: {}", webhookBody, signature);

            // If no signature, this might be a test call - return success for webhook validation
            if (signature == null || signature.isEmpty()) {
                log.info("No PayOS signature found - treating as webhook validation test");
                return ResponseEntity.ok("Webhook endpoint is active and ready to receive PayOS notifications");
            }

            // Verify and process the webhook with signature
            WebhookResponse webhookResponse = paymentService.verifyPaymentWebhook(webhookBody);

            log.info("Successfully processed payment webhook for orderCode: {}, status: {}",
                    webhookResponse.getOrderCode(), webhookResponse.getCode());

            // Here you can add your business logic to handle successful payments
            // For example: update order status, send confirmation emails, etc.
            handleSuccessfulPayment(webhookResponse);

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing PayOS webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to process webhook: " + e.getMessage());
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