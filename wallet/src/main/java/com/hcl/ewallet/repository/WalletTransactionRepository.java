package com.hcl.ewallet.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcl.ewallet.model.TransactionStatus;
import com.hcl.ewallet.model.WalletTransaction;
import com.hcl.ewallet.model.WalletTransactionType;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    /**
     * Find transactions for a specific wallet (source or destination)
     */
    List<WalletTransaction> findBySourceWalletIdOrDestinationWalletId(Long sourceWalletId, Long destinationWalletId);

    /**
     * Find outgoing transactions from a wallet
     */
    List<WalletTransaction> findBySourceWalletId(Long sourceWalletId);

    /**
     * Find incoming transactions to a wallet
     */
    List<WalletTransaction> findByDestinationWalletId(Long destinationWalletId);

    /**
     * Find transactions by status
     */
    List<WalletTransaction> findByStatus(TransactionStatus status);

    /**
     * Find transactions by type
     */
    List<WalletTransaction> findByTransactionType(WalletTransactionType transactionType);

    /**
     * Find transactions within a date range
     */
    List<WalletTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find completed transactions for a merchant wallet within a date range
     */
    List<WalletTransaction> findByDestinationWalletIdAndStatusAndTransactionDateBetween(
        Long destinationWalletId,
        TransactionStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Find transactions by reference number
     */
    boolean existsByReferenceNumber(String referenceNumber);
}
