package com.hcl.ewallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hcl.ewallet.model.TransactionStatus;
import com.hcl.ewallet.model.WalletTransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {

    private Long transactionId;
    private Long sourceWalletId;
    private Long destinationWalletId;
    private BigDecimal amount;
    private WalletTransactionType transactionType;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
    private String description;
    private String referenceNumber;
}
