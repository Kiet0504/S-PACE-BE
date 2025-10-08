package com.example.S_PACE.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOsRequest {

    @NotBlank(message = "Product name is required")
    @JsonProperty("productName")
    private String productName;

    @NotBlank(message = "Description is required")
    @JsonProperty("description")
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 1000, message = "Price must be at least 1000 VND")
    @JsonProperty("price")
    private Integer price;

    @NotBlank(message = "Return URL is required")
    @JsonProperty("returnUrl")
    private String returnUrl;

    @NotBlank(message = "Cancel URL is required")
    @JsonProperty("cancelUrl")
    private String cancelUrl;
}