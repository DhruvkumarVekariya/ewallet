package com.hcl.ewallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMerchantRequest {

    @NotBlank(message = "Merchant name cannot be blank")
    private String name;

    @NotBlank(message = "Merchant ID cannot be blank")
    private String merchantId;

    @NotBlank(message = "Bank account number cannot be blank")
    private String bankAccountNumber;

    @NotBlank(message = "IFSC code cannot be blank")
    private String bankIfscCode;

    @NotBlank(message = "Bank name cannot be blank")
    private String bankName;
}
