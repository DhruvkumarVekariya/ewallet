package com.hcl.ewallet.repository;

import com.hcl.ewallet.model.Transaction;
import com.hcl.ewallet.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByMerchantMerchantIdAndStatusAndTimestampBetween(
        String merchantId,
        TransactionStatus status,
        LocalDateTime start,
        LocalDateTime end
    );
}