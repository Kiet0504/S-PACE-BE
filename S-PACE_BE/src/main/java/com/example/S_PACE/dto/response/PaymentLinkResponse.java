package com.example.S_PACE.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("orderCode")
    private Long orderCode;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("amountPaid")
    private Integer amountPaid;

    @JsonProperty("amountRemaining")
    private Integer amountRemaining;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("transactions")
    private List<TransactionResponse> transactions;

    @JsonProperty("cancellationReason")
    private String cancellationReason;

    @JsonProperty("canceledAt")
    private String canceledAt;
}
