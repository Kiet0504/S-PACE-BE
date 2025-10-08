package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.PayOsRequest;
import com.example.S_PACE.dto.response.PayOsResponse;
import com.example.S_PACE.dto.response.PaymentLinkResponse;
import com.example.S_PACE.dto.response.WebhookResponse;
import com.example.S_PACE.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for PayOS payment integration")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-link")
    @Operation(summary = "Create Payment Link", description = "Create a new payment link using PayOS")
    public ResponseEntity<PayOsResponse> createPaymentLink(@Valid @RequestBody PayOsRequest request) {
        try {
            log.info("Creating payment link for product: {}", request.getProductName());
            PayOsResponse response = paymentService.createPaymentLink(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating payment link: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Get Payment Information", description = "Get payment link information by order code")
    public ResponseEntity<PaymentLinkResponse> getPaymentInformation(@PathVariable Long orderCode) {
        try {
            log.info("Getting payment information for orderCode: {}", orderCode);
            PaymentLinkResponse response = paymentService.getPaymentLinkInformation(orderCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting payment information for orderCode {}: {}", orderCode, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderCode}/cancel")
    @Operation(summary = "Cancel Payment", description = "Cancel a payment link by order code")
    public ResponseEntity<PaymentLinkResponse> cancelPayment(
            @PathVariable Long orderCode,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            String cancellationReason = requestBody != null ?
                requestBody.getOrDefault("reason", "Cancelled by user") : "Cancelled by user";

            log.info("Canceling payment for orderCode: {} with reason: {}", orderCode, cancellationReason);
            PaymentLinkResponse response = paymentService.cancelPaymentLink(orderCode, cancellationReason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error canceling payment for orderCode {}: {}", orderCode, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/webhook-url")
    @Operation(summary = "Get Webhook URL", description = "Get the current webhook URL for PayOS configuration")
    public ResponseEntity<String> getWebhookUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String baseUrl = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            baseUrl += ":" + serverPort;
        }
        baseUrl += contextPath;

        String webhookUrl = baseUrl + "/api/webhooks/payos-payment";
        return ResponseEntity.ok("Your PayOS webhook URL is: " + webhookUrl);
    }

    @PostMapping("/confirm-webhook")
    @Operation(summary = "Confirm Webhook URL", description = "Confirm webhook URL with PayOS")
    public ResponseEntity<String> confirmWebhook(@RequestBody Map<String, String> requestBody) {
        try {
            String webhookUrl = requestBody.get("webhookUrl");
            if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Webhook URL is required");
            }

            log.info("Confirming webhook URL: {}", webhookUrl);
            String result = paymentService.confirmWebhook(webhookUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error confirming webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to confirm webhook: " + e.getMessage());
        }
    }
}