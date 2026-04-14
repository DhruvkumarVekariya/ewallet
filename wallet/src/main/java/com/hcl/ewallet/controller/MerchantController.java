package com.hcl.ewallet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcl.ewallet.dto.CreateMerchantRequest;
import com.hcl.ewallet.dto.MerchantResponse;
import com.hcl.ewallet.service.MerchantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);

    @Autowired
    private MerchantService merchantService;

    @PostMapping
    public ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        logger.info("API: Create merchant request for merchantId: {}", request.getMerchantId());
        MerchantResponse response = merchantService.createMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<MerchantResponse> getMerchantByMerchantId(@PathVariable String merchantId) {
        logger.info("API: Get merchant by merchantId: {}", merchantId);
        MerchantResponse response = merchantService.getMerchantByMerchantId(merchantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<MerchantResponse> getMerchantById(@PathVariable Long id) {
        logger.info("API: Get merchant by id: {}", id);
        MerchantResponse response = merchantService.getMerchant(id);
        return ResponseEntity.ok(response);
    }
}
