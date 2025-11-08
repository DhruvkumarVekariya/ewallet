package com.hcl.ewallet.service.impl;

import com.hcl.ewallet.model.Merchant;
import com.hcl.ewallet.model.Settlement;
import com.hcl.ewallet.model.SettlementStatus;
import com.hcl.ewallet.repository.MerchantRepository;
import com.hcl.ewallet.repository.SettlementRepository;
import com.hcl.ewallet.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import com.hcl.ewallet.repository.TransactionRepository;
import com.hcl.ewallet.model.TransactionStatus;
import com.hcl.ewallet.gateway.PaymentGateway;
import com.hcl.ewallet.gateway.GatewayResponse;
import com.hcl.ewallet.repository.TransactionLedgerRepository;
import com.hcl.ewallet.model.TransactionLedger;

@Service
public class SettlementServiceImpl implements SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final java.math.BigDecimal TRANSACTION_FEE = java.math.BigDecimal.valueOf(10);

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private TransactionLedgerRepository transactionLedgerRepository;

    @Override
    @Transactional
    public Settlement initiateSettlement(String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId);
        if (merchant == null) {
            throw new RuntimeException("Merchant not found: " + merchantId);
        }

        BigDecimal amountToSettle = calculateSettlementAmount(merchant);

        Settlement settlement = new Settlement();
        settlement.setMerchant(merchant);
        settlement.setTotalAmount(amountToSettle);
        settlement.setSettlementDate(LocalDateTime.now());
        settlement.setStatus(SettlementStatus.PENDING);
        settlement.setReferenceNumber(generateReferenceNumber());
        
        return settlementRepository.save(settlement);
    }

    @Override
    @Transactional
    public void processPendingSettlements() {
        List<Settlement> pendingSettlements = settlementRepository
            .findByStatusAndSettlementDateBefore(
                SettlementStatus.PENDING,
                LocalDateTime.now()
            );

        for (Settlement settlement : pendingSettlements) {
            processSettlement(settlement);
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processDayEndSettlements() {
        List<Merchant> merchants = merchantRepository.findAll();
        for (Merchant merchant : merchants) {
            try {
                initiateSettlement(merchant.getMerchantId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private BigDecimal calculateSettlementAmount(Merchant merchant) {

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(LocalTime.MAX);

        List<com.hcl.ewallet.model.Transaction> txns = transactionRepository
            .findByMerchantMerchantIdAndStatusAndTimestampBetween(
                merchant.getMerchantId(),
                TransactionStatus.COMPLETED,
                startOfDay,
                endOfDay
            );

        BigDecimal total = BigDecimal.ZERO;
        for (com.hcl.ewallet.model.Transaction t : txns) {
            if (t.getAmount() != null) {
                total = total.add(t.getAmount());
            }
        }

        // Wallet fee per transaction
        if (!txns.isEmpty()) {
            java.math.BigDecimal feeTotal = TRANSACTION_FEE.multiply(java.math.BigDecimal.valueOf(txns.size()));
            total = total.subtract(feeTotal);
        }

        // Avoiding negative settlement
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return total;
    }

    private String generateReferenceNumber() {
        return "STL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void processSettlement(Settlement settlement) {
        try {
            settlement.setStatus(SettlementStatus.PROCESSING);
            settlementRepository.save(settlement);

            // Dummy gateway
            GatewayResponse resp = paymentGateway.transferToBank(
                settlement.getMerchant().getBankAccountNumber(),
                settlement.getMerchant().getBankIfscCode(),
                settlement.getMerchant().getBankName(),
                settlement.getTotalAmount(),
                settlement.getReferenceNumber()
            );

            if (resp != null && resp.isSuccess()) {
                settlement.setStatus(SettlementStatus.COMPLETED);
                settlement.setRemarks("ProviderRef: " + resp.getProviderReference());
                settlement.setProcessedAt(LocalDateTime.now());
                // create a transaction ledger entry for this settlement
                try {
                    TransactionLedger ledger = new TransactionLedger();
                    ledger.setMerchant(settlement.getMerchant());
                    ledger.setAmount(settlement.getTotalAmount());
                    ledger.setReferenceNumber(settlement.getReferenceNumber());
                    ledger.setTimestamp(LocalDateTime.now());
                    ledger.setRemarks("Settled to bank. ProviderRef: " + resp.getProviderReference());
                    transactionLedgerRepository.save(ledger);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String msg = (resp != null) ? resp.getMessage() : "Unknown gateway error";
                settlement.setStatus(SettlementStatus.FAILED);
                settlement.setRemarks("Gateway failure: " + msg);
            }
        } catch (Exception e) {
            settlement.setStatus(SettlementStatus.FAILED);
            settlement.setRemarks("Failed to process: " + e.getMessage());
        } finally {
            settlement.setUpdatedAt(LocalDateTime.now());
            settlementRepository.save(settlement);
        }
    }
}