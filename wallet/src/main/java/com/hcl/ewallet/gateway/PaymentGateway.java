package com.hcl.ewallet.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {
    /**
     * Transfer amount to provided bank account.
     * @return GatewayResponse with success flag and provider reference
     */
    GatewayResponse transferToBank(String accountNumber, String ifsc, String bankName, BigDecimal amount, String reference);
}
