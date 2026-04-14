package com.hcl.ewallet.controller;

import java.util.List;

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

import com.hcl.ewallet.dto.LoadMoneyRequest;
import com.hcl.ewallet.dto.TransferMoneyRequest;
import com.hcl.ewallet.dto.WalletResponse;
import com.hcl.ewallet.dto.WalletTransactionResponse;
import com.hcl.ewallet.service.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    @Autowired
    private WalletService walletService;

    /**
     * Load money into user wallet
     * POST /api/wallets/load-money
     */
    @PostMapping("/load-money")
    public ResponseEntity<WalletTransactionResponse> loadMoney(@Valid @RequestBody LoadMoneyRequest request) {
        logger.info("API: Load money request for user: {}, amount: {}", request.getUserId(), request.getAmount());
        try {
            WalletTransactionResponse response = walletService.loadMoney(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error loading money for user: {}", request.getUserId(), e);
            throw e;
        }
    }

    /**
     * Transfer money from user wallet to merchant wallet
     * POST /api/wallets/transfer-to-merchant
     */
    @PostMapping("/transfer-to-merchant")
    public ResponseEntity<WalletTransactionResponse> transferToMerchant(@Valid @RequestBody TransferMoneyRequest request) {
        logger.info("API: Transfer money request from user: {} to merchant: {}, amount: {}",
            request.getUserId(), request.getMerchantId(), request.getAmount());
        try {
            WalletTransactionResponse response = walletService.transferToMerchant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error transferring money from user: {} to merchant: {}",
                request.getUserId(), request.getMerchantId(), e);
            throw e;
        }
    }

    /**
     * Get user wallet by user ID
     * GET /api/wallets/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getUserWallet(@PathVariable Long userId) {
        logger.info("API: Get user wallet for user: {}", userId);
        try {
            WalletResponse response = walletService.getUserWallet(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting user wallet for user: {}", userId, e);
            throw e;
        }
    }

    /**
     * Get merchant wallet by merchant ID
     * GET /api/wallets/merchant/{merchantId}
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<WalletResponse> getMerchantWallet(@PathVariable Long merchantId) {
        logger.info("API: Get merchant wallet for merchant: {}", merchantId);
        try {
            WalletResponse response = walletService.getMerchantWallet(merchantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting merchant wallet for merchant: {}", merchantId, e);
            throw e;
        }
    }

    /**
     * Get wallet by wallet ID
     * GET /api/wallets/{walletId}
     */
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long walletId) {
        logger.info("API: Get wallet: {}", walletId);
        try {
            WalletResponse response = walletService.getWallet(walletId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting wallet: {}", walletId, e);
            throw e;
        }
    }

    /**
     * Get wallet transaction history
     * GET /api/wallets/{walletId}/transactions
     */
    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getWalletTransactionHistory(@PathVariable Long walletId) {
        logger.info("API: Get transaction history for wallet: {}", walletId);
        try {
            List<WalletTransactionResponse> response = walletService.getWalletTransactionHistory(walletId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting transaction history for wallet: {}", walletId, e);
            throw e;
        }
    }

    /**
     * Create wallet for a new user
     * POST /api/wallets/user/{userId}
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> createUserWallet(@PathVariable Long userId) {
        logger.info("API: Create user wallet for user: {}", userId);
        try {
            WalletResponse response = walletService.createUserWallet(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating user wallet for user: {}", userId, e);
            throw e;
        }
    }

    /**
     * Create wallet for a new merchant
     * POST /api/wallets/merchant/{merchantId}
     */
    @PostMapping("/merchant/{merchantId}")
    public ResponseEntity<WalletResponse> createMerchantWallet(@PathVariable Long merchantId) {
        logger.info("API: Create merchant wallet for merchant: {}", merchantId);
        try {
            WalletResponse response = walletService.createMerchantWallet(merchantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating merchant wallet for merchant: {}", merchantId, e);
            throw e;
        }
    }
}
