package com.hcl.ewallet.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcl.ewallet.dto.CreateMerchantRequest;
import com.hcl.ewallet.dto.MerchantResponse;
import com.hcl.ewallet.model.Merchant;
import com.hcl.ewallet.repository.MerchantRepository;
import com.hcl.ewallet.service.MerchantService;
import com.hcl.ewallet.service.WalletService;

@Service
@Transactional
public class MerchantServiceImpl implements MerchantService {

    private static final Logger logger = LoggerFactory.getLogger(MerchantServiceImpl.class);

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private WalletService walletService;

    @Override
    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        logger.info("Creating merchant: {}", request.getName());

        if (merchantRepository.findByMerchantId(request.getMerchantId()) != null) {
            throw new RuntimeException("Merchant already exists: " + request.getMerchantId());
        }

        Merchant merchant = new Merchant();
        merchant.setName(request.getName());
        merchant.setMerchantId(request.getMerchantId());
        merchant.setBankAccountNumber(request.getBankAccountNumber());
        merchant.setBankIfscCode(request.getBankIfscCode());
        merchant.setBankName(request.getBankName());
        merchant.setActive(true);

        Merchant savedMerchant = merchantRepository.save(merchant);
        logger.info("Merchant created successfully. MerchantId: {}", savedMerchant.getMerchantId());

        walletService.createMerchantWallet(savedMerchant.getId());
        logger.info("Merchant wallet created automatically for merchant: {}", savedMerchant.getMerchantId());

        return mapToMerchantResponse(savedMerchant);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchant(Long merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null");
        }

        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantId));

        return mapToMerchantResponse(merchant);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchantByMerchantId(String merchantId) {
        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("Merchant identifier cannot be blank");
        }

        Merchant merchant = merchantRepository.findByMerchantId(merchantId);
        if (merchant == null) {
            throw new RuntimeException("Merchant not found: " + merchantId);
        }

        return mapToMerchantResponse(merchant);
    }

    private MerchantResponse mapToMerchantResponse(Merchant merchant) {
        MerchantResponse response = new MerchantResponse();
        response.setId(merchant.getId());
        response.setName(merchant.getName());
        response.setMerchantId(merchant.getMerchantId());
        response.setBankAccountNumber(merchant.getBankAccountNumber());
        response.setBankIfscCode(merchant.getBankIfscCode());
        response.setBankName(merchant.getBankName());
        response.setActive(merchant.isActive());
        return response;
    }
}
