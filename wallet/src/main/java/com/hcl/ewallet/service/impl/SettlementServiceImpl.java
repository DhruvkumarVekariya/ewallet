package com.hcl.ewallet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementServiceImpl.class);

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
        logger.info("Initiating settlement process for merchant: {}", merchantId);
        
        Merchant merchant = merchantRepository.findByMerchantId(merchantId);
        if (merchant == null) {
            logger.error("Merchant not found with ID: {}", merchantId);
            throw new RuntimeException("Merchant not found: " + merchantId);
        }

        logger.debug("Calculating settlement amount for merchant: {}", merchantId);
        BigDecimal amountToSettle = calculateSettlementAmount(merchant);
        logger.info("Settlement amount calculated for merchant {}: {}", merchantId, amountToSettle);

        Settlement settlement = new Settlement();
        settlement.setMerchant(merchant);
        settlement.setTotalAmount(amountToSettle);
        settlement.setSettlementDate(LocalDateTime.now());
        settlement.setStatus(SettlementStatus.PENDING);
        settlement.setReferenceNumber(generateReferenceNumber());
        
        logger.info("Saving settlement for merchant: {}, reference number: {}", merchantId, settlement.getReferenceNumber());
        Settlement savedSettlement = settlementRepository.save(settlement);
        logger.debug("Settlement saved successfully with ID: {}", savedSettlement.getId());
        
        return savedSettlement;
    }

    @Override
    @Transactional
    public void processPendingSettlements() {
        logger.info("Starting to process pending settlements");
        
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Fetching pending settlements before: {}", now);
        
        List<Settlement> pendingSettlements = settlementRepository
            .findByStatusAndSettlementDateBefore(
                SettlementStatus.PENDING,
                now
            );
        
        logger.info("Found {} pending settlements to process", pendingSettlements.size());

        for (Settlement settlement : pendingSettlements) {
            try {
                logger.debug("Processing settlement ID: {}", settlement.getId());
                processSettlement(settlement);
                logger.info("Successfully processed settlement ID: {}", settlement.getId());
            } catch (Exception e) {
                logger.error("Error processing settlement ID: {}", settlement.getId(), e);
                throw e;
            }
        }
        
        logger.info("Completed processing all pending settlements");
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processDayEndSettlements() {
        logger.info("Starting day-end settlement process");
        
        List<Merchant> merchants = merchantRepository.findAll();
        logger.info("Found {} merchants for day-end settlement", merchants.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Merchant merchant : merchants) {
            try {
                logger.debug("Processing day-end settlement for merchant: {}", merchant.getMerchantId());
                initiateSettlement(merchant.getMerchantId());
                successCount++;
                logger.info("Successfully processed day-end settlement for merchant: {}", merchant.getMerchantId());
            } catch (Exception e) {
                failureCount++;
                logger.error("Failed to process day-end settlement for merchant: {}", merchant.getMerchantId(), e);
            }
        }
        
        logger.info("Day-end settlement process completed. Success: {}, Failures: {}", successCount, failureCount);
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