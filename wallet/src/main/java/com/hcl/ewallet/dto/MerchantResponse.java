package com.hcl.ewallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {

    private Long id;
    private String name;
    private String merchantId;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String bankName;
    private boolean active;
}
