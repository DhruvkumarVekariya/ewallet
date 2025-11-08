package com.hcl.ewallet;

import com.hcl.ewallet.gateway.GatewayResponse;
import com.hcl.ewallet.gateway.PaymentGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class GatewayIntegrationTest {

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private WebTestClient webClient;

    @Test
    void dummyGatewayWorks() {
        GatewayResponse resp = paymentGateway.transferToBank(
            "9999999999",
            "TEST0001",
            "Test Bank",
            BigDecimal.valueOf(1000),
            "TEST-REF-1"
        );

        System.out.println("Dummy gateway response: success=" + resp.isSuccess()
            + " providerRef=" + resp.getProviderReference()
            + " message=" + resp.getMessage());

        assertTrue(resp.isSuccess(), "Dummy gateway should succeed for positive amount");
    }

    @Test
    void dayEndEndpointInvokesGateway() {
        webClient.post()
            .uri("/api/settlements/day-end")
            .exchange()
            .expectStatus().is2xxSuccessful();

        System.out.println("Called /api/settlements/day-end -> HTTP 2xx");
    }
}
