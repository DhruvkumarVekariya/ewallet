package com.hcl.ewallet.gateway;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DummyPaymentGateway implements PaymentGateway {

    @Override
    public GatewayResponse transferToBank(String accountNumber, String ifsc, String bankName, BigDecimal amount, String reference) {
        // Dummy implementation: succeed for amounts > 0, fail otherwise
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new GatewayResponse(false, null, "Invalid amount");
        }

        // Simulate provider reference
        String providerRef = "DUMMY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // In a real implementation you'd call the bank/payment provider API here.
        return new GatewayResponse(true, providerRef, "Transfer queued/simulated successfully");
    }
}
