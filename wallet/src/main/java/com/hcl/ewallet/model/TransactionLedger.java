package com.hcl.ewallet.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Merchant merchant;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String referenceNumber;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String remarks;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
