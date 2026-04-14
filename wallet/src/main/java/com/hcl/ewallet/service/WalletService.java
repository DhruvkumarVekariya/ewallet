package com.hcl.ewallet.service;

import java.util.List;

import com.hcl.ewallet.dto.LoadMoneyRequest;
import com.hcl.ewallet.dto.TransferMoneyRequest;
import com.hcl.ewallet.dto.WalletResponse;
import com.hcl.ewallet.dto.WalletTransactionResponse;

public interface WalletService {

    /**
     * Load money into user wallet
     */
    WalletTransactionResponse loadMoney(LoadMoneyRequest request);

    /**
     * Transfer money from user wallet to merchant wallet
     */
    WalletTransactionResponse transferToMerchant(TransferMoneyRequest request);

    /**
     * Get user wallet by user ID
     */
    WalletResponse getUserWallet(Long userId);

    /**
     * Get merchant wallet by merchant ID
     */
    WalletResponse getMerchantWallet(Long merchantId);

    /**
     * Get wallet balance by wallet ID
     */
    WalletResponse getWallet(Long walletId);

    /**
     * Get transaction history for a wallet
     */
    List<WalletTransactionResponse> getWalletTransactionHistory(Long walletId);

    /**
     * Create wallet for a new user
     */
    WalletResponse createUserWallet(Long userId);

    /**
     * Create wallet for a new merchant
     */
    WalletResponse createMerchantWallet(Long merchantId);
}
