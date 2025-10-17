package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.PayOsRequest;
import com.example.S_PACE.dto.response.PayOsResponse;
import com.example.S_PACE.dto.response.PaymentLinkResponse;
import com.example.S_PACE.dto.response.WebhookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PayOS payOS;

    public PayOsResponse createPaymentLink(PayOsRequest request) {
        try {
            long orderCode = System.currentTimeMillis() / 1000;

            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(request.getProductName())
                    .quantity(1)
                    .price(request.getPrice().longValue())
                    .build();

            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(request.getPrice().longValue())
                    .description(request.getDescription())
                    .returnUrl(request.getReturnUrl())
                    .cancelUrl(request.getCancelUrl())
                    .item(item)
                    .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            return new PayOsResponse(
                    data.getBin(),
                    data.getAccountNumber(),
                    data.getAccountName(),
                    data.getAmount().intValue(),
                    data.getDescription(),
                    data.getOrderCode(),
                    data.getCurrency(),
                    data.getPaymentLinkId(),
                    data.getStatus().toString(),
                    data.getCheckoutUrl(),
                    data.getQrCode()
            );

        } catch (Exception e) {
            log.error("Error creating payment link: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment link: " + e.getMessage());
        }
    }

    public PaymentLinkResponse getPaymentLinkInformation(Long orderCode) {
        try {
            PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);

            return new PaymentLinkResponse(
                    paymentLink.getId(),
                    paymentLink.getOrderCode(),
                    paymentLink.getAmount().intValue(),
                    paymentLink.getAmountPaid().intValue(),
                    paymentLink.getAmountRemaining().intValue(),
                    paymentLink.getStatus().toString(),
                    paymentLink.getCreatedAt().toString(),
                    null, // transactions - implement mapping if needed
                    paymentLink.getCancellationReason(),
                    paymentLink.getCanceledAt() != null ? paymentLink.getCanceledAt().toString() : null
            );

        } catch (Exception e) {
            log.error("Error getting payment link information for orderCode {}: {}", orderCode, e.getMessage(), e);
            throw new RuntimeException("Failed to get payment link information: " + e.getMessage());
        }
    }

    public PaymentLinkResponse cancelPaymentLink(Long orderCode, String cancellationReason) {
        try {
            PaymentLink paymentLink = payOS.paymentRequests().cancel(orderCode, cancellationReason);

            return new PaymentLinkResponse(
                    paymentLink.getId(),
                    paymentLink.getOrderCode(),
                    paymentLink.getAmount().intValue(),
                    paymentLink.getAmountPaid().intValue(),
                    paymentLink.getAmountRemaining().intValue(),
                    paymentLink.getStatus().toString(),
                    paymentLink.getCreatedAt().toString(),
                    null, // transactions
                    paymentLink.getCancellationReason(),
                    paymentLink.getCanceledAt() != null ? paymentLink.getCanceledAt().toString() : null
            );

        } catch (Exception e) {
            log.error("Error canceling payment link for orderCode {}: {}", orderCode, e.getMessage(), e);
            throw new RuntimeException("Failed to cancel payment link: " + e.getMessage());
        }
    }

    public String confirmWebhook(String webhookUrl) {
        try {
            return payOS.webhooks().confirm(webhookUrl).getWebhookUrl();
        } catch (Exception e) {
            log.error("Error confirming webhook URL {}: {}", webhookUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to confirm webhook: " + e.getMessage());
        }
    }

    public WebhookResponse verifyPaymentWebhook(Object webhookBody) {
        try {
            WebhookData data = payOS.webhooks().verify(webhookBody);

            return new WebhookResponse(
                    data.getOrderCode(),
                    data.getAmount().intValue(),
                    data.getDescription(),
                    data.getAccountNumber(),
                    data.getReference(),
                    data.getTransactionDateTime().toString(),
                    data.getCurrency(),
                    data.getPaymentLinkId(),
                    data.getCode(),
                    data.getDesc(),
                    data.getCounterAccountBankId(),
                    data.getCounterAccountBankName(),
                    data.getCounterAccountName(),
                    data.getCounterAccountNumber(),
                    data.getVirtualAccountName(),
                    data.getVirtualAccountNumber()
            );

        } catch (Exception e) {
            log.error("Error verifying payment webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to verify payment webhook: " + e.getMessage());
        }
    }
}
