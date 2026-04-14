package com.hcl.ewallet.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferMoneyRequest {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Merchant ID cannot be null")
    private Long merchantId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String description;
}
