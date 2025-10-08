package com.example.S_PACE.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("description")
    private String description;

    @JsonProperty("transactionDateTime")
    private String transactionDateTime;

    @JsonProperty("virtualAccountName")
    private String virtualAccountName;

    @JsonProperty("virtualAccountNumber")
    private String virtualAccountNumber;

    @JsonProperty("counterAccountBankId")
    private String counterAccountBankId;

    @JsonProperty("counterAccountBankName")
    private String counterAccountBankName;

    @JsonProperty("counterAccountName")
    private String counterAccountName;

    @JsonProperty("counterAccountNumber")
    private String counterAccountNumber;
}