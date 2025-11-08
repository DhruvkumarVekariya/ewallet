package com.hcl.ewallet.service;

import com.hcl.ewallet.gateway.GatewayResponse;
import com.hcl.ewallet.gateway.PaymentGateway;
import com.hcl.ewallet.model.Merchant;
import com.hcl.ewallet.model.Settlement;
import com.hcl.ewallet.model.SettlementStatus;
import com.hcl.ewallet.model.Transaction;
import com.hcl.ewallet.model.TransactionLedger;
import com.hcl.ewallet.model.TransactionStatus;
import com.hcl.ewallet.repository.MerchantRepository;
import com.hcl.ewallet.repository.SettlementRepository;
import com.hcl.ewallet.repository.TransactionLedgerRepository;
import com.hcl.ewallet.repository.TransactionRepository;
import com.hcl.ewallet.service.impl.SettlementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettlementServiceTest {

    @InjectMocks
    private SettlementServiceImpl settlementService;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private TransactionLedgerRepository transactionLedgerRepository;

    // No global stubbings here to avoid unnecessary stubbing exceptions.

    @Test
    void initiateSettlement_success() {
        Merchant m = new Merchant();
        m.setMerchantId("M1");
        m.setBankAccountNumber("111111");
        m.setBankIfscCode("IFSC");
        m.setBankName("BankX");

    when(merchantRepository.findByMerchantId("M1")).thenReturn(m);

    // make save return the saved entity
    when(settlementRepository.save(any(Settlement.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction t1 = new Transaction();
        t1.setMerchant(m);
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setStatus(TransactionStatus.COMPLETED);
        t1.setTimestamp(LocalDate.now().minusDays(1).atTime(10, 0));

        Transaction t2 = new Transaction();
        t2.setMerchant(m);
        t2.setAmount(BigDecimal.valueOf(200));
        t2.setStatus(TransactionStatus.COMPLETED);
        t2.setTimestamp(LocalDate.now().minusDays(1).atTime(12, 0));

        when(transactionRepository.findByMerchantMerchantIdAndStatusAndTimestampBetween(
            eq("M1"), eq(TransactionStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(t1, t2));

        Settlement res = settlementService.initiateSettlement("M1");

    // total = 100 + 200 - (10 * 2) = 280
        assertNotNull(res);
    assertEquals(new BigDecimal("280"), res.getTotalAmount());
        assertEquals(SettlementStatus.PENDING, res.getStatus());
        assertNotNull(res.getReferenceNumber());
        assertTrue(res.getReferenceNumber().startsWith("STL-"));
        verify(settlementRepository, times(1)).save(any(Settlement.class));
    }

    @Test
    void initiateSettlement_merchantNotFound_throws() {
        when(merchantRepository.findByMerchantId("NOPE")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> settlementService.initiateSettlement("NOPE"));
        verifyNoInteractions(transactionRepository);
        verify(settlementRepository, never()).save(any());
    }

    @Test
    void processPendingSettlements_gatewaySuccess_createsLedgerAndCompletes() {
        Merchant m = new Merchant();
        m.setMerchantId("M2");
        m.setBankAccountNumber("22222");
        m.setBankIfscCode("IFSC2");
        m.setBankName("BankY");

        Settlement s = new Settlement();
        s.setMerchant(m);
        s.setTotalAmount(BigDecimal.valueOf(150));
        s.setStatus(SettlementStatus.PENDING);
        s.setSettlementDate(LocalDateTime.now().minusHours(2));
        s.setReferenceNumber("REF-1");

        when(settlementRepository.findByStatusAndSettlementDateBefore(eq(SettlementStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(List.of(s));

        // stub saves
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionLedgerRepository.save(any(TransactionLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        when(paymentGateway.transferToBank(anyString(), anyString(), anyString(), any(BigDecimal.class), anyString()))
            .thenReturn(new GatewayResponse(true, "PR-1", "ok"));

        settlementService.processPendingSettlements();

        // After processing, settlement instance should be updated to COMPLETED
        assertEquals(SettlementStatus.COMPLETED, s.getStatus());
        assertNotNull(s.getProcessedAt());
        assertTrue(s.getRemarks().contains("ProviderRef: ") || s.getRemarks().contains("ProviderRef"));

        // Verify ledger saved
        ArgumentCaptor<TransactionLedger> ledgerCaptor = ArgumentCaptor.forClass(TransactionLedger.class);
        verify(transactionLedgerRepository, times(1)).save(ledgerCaptor.capture());
        TransactionLedger ledger = ledgerCaptor.getValue();
        assertEquals(m, ledger.getMerchant());
        assertEquals(s.getTotalAmount(), ledger.getAmount());

        verify(settlementRepository, atLeast(2)).save(any(Settlement.class));
    }

    @Test
    void processPendingSettlements_gatewayFailure_marksFailed() {
        Merchant m = new Merchant();
        m.setMerchantId("M3");
        m.setBankAccountNumber("33333");
        m.setBankIfscCode("IFSC3");
        m.setBankName("BankZ");

        Settlement s = new Settlement();
        s.setMerchant(m);
        s.setTotalAmount(BigDecimal.valueOf(50));
        s.setStatus(SettlementStatus.PENDING);
        s.setSettlementDate(LocalDateTime.now().minusHours(2));
        s.setReferenceNumber("REF-2");

        when(settlementRepository.findByStatusAndSettlementDateBefore(eq(SettlementStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(List.of(s));

        // stub save so the final save doesn't cause unnecessary stubbing in other tests
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(inv -> inv.getArgument(0));

        when(paymentGateway.transferToBank(anyString(), anyString(), anyString(), any(BigDecimal.class), anyString()))
            .thenReturn(new GatewayResponse(false, null, "Insufficient funds"));

        settlementService.processPendingSettlements();

        assertEquals(SettlementStatus.FAILED, s.getStatus());
        assertTrue(s.getRemarks().contains("Gateway failure"));
        verify(transactionLedgerRepository, never()).save(any(TransactionLedger.class));
    }
}
