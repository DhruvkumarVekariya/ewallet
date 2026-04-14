package com.hcl.ewallet.repository;

import com.hcl.ewallet.model.Wallet;
import com.hcl.ewallet.model.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Find wallet for a specific user
     */
    Optional<Wallet> findByUserIdAndWalletType(Long userId, WalletType walletType);

    /**
     * Find wallet for a specific merchant
     */
    Optional<Wallet> findByMerchantIdAndWalletType(Long merchantId, WalletType walletType);

    /**
     * Check if user wallet exists
     */
    boolean existsByUserIdAndWalletType(Long userId, WalletType walletType);

    /**
     * Check if merchant wallet exists
     */
    boolean existsByMerchantIdAndWalletType(Long merchantId, WalletType walletType);

    /**
     * Find merchant wallet using external merchant identifier
     */
    java.util.Optional<Wallet> findByMerchantMerchantIdAndWalletType(String merchantId, WalletType walletType);

    /**
     * Find all wallets for a user
     */
    List<Wallet> findByUserId(Long userId);

    /**
     * Find all wallets for a merchant
     */
    List<Wallet> findByMerchantId(Long merchantId);
}
