package com.example.S_PACE.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOsResponse {

    @JsonProperty("bin")
    private String bin;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("orderCode")
    private Long orderCode;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("paymentLinkId")
    private String paymentLinkId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("checkoutUrl")
    private String checkoutUrl;

    @JsonProperty("qrCode")
    private String qrCode;
}