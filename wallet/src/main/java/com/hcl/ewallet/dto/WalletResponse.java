package com.hcl.ewallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hcl.ewallet.model.WalletType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private Long walletId;
    private Long userId;
    private Long merchantId;
    private WalletType walletType;
    private BigDecimal balance;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
