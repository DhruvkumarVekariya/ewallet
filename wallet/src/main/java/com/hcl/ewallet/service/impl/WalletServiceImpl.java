package com.hcl.ewallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcl.ewallet.dto.LoadMoneyRequest;
import com.hcl.ewallet.dto.TransferMoneyRequest;
import com.hcl.ewallet.dto.WalletResponse;
import com.hcl.ewallet.dto.WalletTransactionResponse;
import com.hcl.ewallet.model.Merchant;
import com.hcl.ewallet.model.TransactionStatus;
import com.hcl.ewallet.model.User;
import com.hcl.ewallet.model.Wallet;
import com.hcl.ewallet.model.WalletTransaction;
import com.hcl.ewallet.model.WalletTransactionType;
import com.hcl.ewallet.model.WalletType;
import com.hcl.ewallet.repository.MerchantRepository;
import com.hcl.ewallet.repository.UserRepository;
import com.hcl.ewallet.repository.WalletRepository;
import com.hcl.ewallet.repository.WalletTransactionRepository;
import com.hcl.ewallet.service.WalletService;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Override
    public WalletTransactionResponse loadMoney(LoadMoneyRequest request) {
        logger.info("Loading money for user: {}, amount: {}", request.getUserId(), request.getAmount());

        // Validate amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // Get user wallet
        Wallet userWallet = walletRepository.findByUserIdAndWalletType(request.getUserId(), WalletType.USER_WALLET)
            .orElseThrow(() -> new RuntimeException("User wallet not found for user: " + request.getUserId()));

        // Create a dummy wallet to act as source for load money (representing external payment system)
        Wallet sourceWallet = userWallet; // For now, we'll treat it as same wallet

        // Create wallet transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setSourceWallet(sourceWallet);
        transaction.setDestinationWallet(userWallet);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(WalletTransactionType.LOAD_MONEY);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "Money loaded");
        transaction.setReferenceNumber(generateReferenceNumber());

        // Update wallet balance
        userWallet.setBalance(userWallet.getBalance().add(request.getAmount()));
        userWallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(userWallet);
        walletTransactionRepository.save(transaction);

        logger.info("Successfully loaded money. User: {}, Amount: {}, Reference: {}",
            request.getUserId(), request.getAmount(), transaction.getReferenceNumber());

        return mapToTransactionResponse(transaction);
    }

    @Override
    public WalletTransactionResponse transferToMerchant(TransferMoneyRequest request) {
        logger.info("Transferring money from user: {} to merchant: {}, amount: {}",
            request.getUserId(), request.getMerchantId(), request.getAmount());

        // Validate amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // Get user wallet
        Wallet userWallet = walletRepository.findByUserIdAndWalletType(request.getUserId(), WalletType.USER_WALLET)
            .orElseThrow(() -> new RuntimeException("User wallet not found for user: " + request.getUserId()));

        // Get merchant wallet
        Wallet merchantWallet = walletRepository.findByMerchantIdAndWalletType(request.getMerchantId(), WalletType.MERCHANT_WALLET)
            .orElseThrow(() -> new RuntimeException("Merchant wallet not found for merchant: " + request.getMerchantId()));

        // Check balance
        if (userWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance in user wallet");
        }

        // Create wallet transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setSourceWallet(userWallet);
        transaction.setDestinationWallet(merchantWallet);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(WalletTransactionType.TRANSFER_TO_MERCHANT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "Transfer to merchant");
        transaction.setReferenceNumber(generateReferenceNumber());

        // Update source wallet balance
        userWallet.setBalance(userWallet.getBalance().subtract(request.getAmount()));
        userWallet.setUpdatedAt(LocalDateTime.now());

        // Update destination wallet balance
        merchantWallet.setBalance(merchantWallet.getBalance().add(request.getAmount()));
        merchantWallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(userWallet);
        walletRepository.save(merchantWallet);
        walletTransactionRepository.save(transaction);

        logger.info("Successfully transferred money. User: {}, Merchant: {}, Amount: {}, Reference: {}",
            request.getUserId(), request.getMerchantId(), request.getAmount(), transaction.getReferenceNumber());

        return mapToTransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getUserWallet(Long userId) {
        logger.debug("Getting user wallet for user: {}", userId);

        Wallet wallet = walletRepository.findByUserIdAndWalletType(userId, WalletType.USER_WALLET)
            .orElseThrow(() -> new RuntimeException("User wallet not found for user: " + userId));

        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getMerchantWallet(Long merchantId) {
        logger.debug("Getting merchant wallet for merchant: {}", merchantId);

        Wallet wallet = walletRepository.findByMerchantIdAndWalletType(merchantId, WalletType.MERCHANT_WALLET)
            .orElseThrow(() -> new RuntimeException("Merchant wallet not found for merchant: " + merchantId));

        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long walletId) {
        logger.debug("Getting wallet: {}", walletId);

        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID cannot be null");
        }

        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));

        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getWalletTransactionHistory(Long walletId) {
        logger.debug("Getting transaction history for wallet: {}", walletId);

        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID cannot be null");
        }

        // Verify wallet exists
        walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));

        List<WalletTransaction> transactions = walletTransactionRepository
            .findBySourceWalletIdOrDestinationWalletId(walletId, walletId);

        return transactions.stream()
            .map(this::mapToTransactionResponse)
            .collect(Collectors.toList());
    }

    @Override
    public WalletResponse createUserWallet(Long userId) {
        logger.info("Creating user wallet for user: {}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Check if user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Check if wallet already exists
        if (walletRepository.existsByUserIdAndWalletType(userId, WalletType.USER_WALLET)) {
            throw new RuntimeException("Wallet already exists for user: " + userId);
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletType(WalletType.USER_WALLET);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setActive(true);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);
        logger.info("User wallet created successfully. User: {}, Wallet ID: {}", userId, savedWallet.getId());

        return mapToWalletResponse(savedWallet);
    }

    @Override
    public WalletResponse createMerchantWallet(Long merchantId) {
        logger.info("Creating merchant wallet for merchant: {}", merchantId);

        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null");
        }

        // Check if merchant exists
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantId));

        // Check if wallet already exists
        if (walletRepository.existsByMerchantIdAndWalletType(merchantId, WalletType.MERCHANT_WALLET)) {
            throw new RuntimeException("Wallet already exists for merchant: " + merchantId);
        }

        Wallet wallet = new Wallet();
        wallet.setMerchant(merchant);
        wallet.setWalletType(WalletType.MERCHANT_WALLET);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setActive(true);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);
        logger.info("Merchant wallet created successfully. Merchant: {}, Wallet ID: {}", merchantId, savedWallet.getId());

        return mapToWalletResponse(savedWallet);
    }

    // Helper methods

    private WalletResponse mapToWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setWalletId(wallet.getId());
        response.setUserId(wallet.getUser() != null ? wallet.getUser().getId() : null);
        response.setMerchantId(wallet.getMerchant() != null ? wallet.getMerchant().getId() : null);
        response.setWalletType(wallet.getWalletType());
        response.setBalance(wallet.getBalance());
        response.setActive(wallet.isActive());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }

    private WalletTransactionResponse mapToTransactionResponse(WalletTransaction transaction) {
        WalletTransactionResponse response = new WalletTransactionResponse();
        response.setTransactionId(transaction.getId());
        response.setSourceWalletId(transaction.getSourceWallet().getId());
        response.setDestinationWalletId(transaction.getDestinationWallet().getId());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(transaction.getTransactionType());
        response.setStatus(transaction.getStatus());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setDescription(transaction.getDescription());
        response.setReferenceNumber(transaction.getReferenceNumber());
        return response;
    }

    private String generateReferenceNumber() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
