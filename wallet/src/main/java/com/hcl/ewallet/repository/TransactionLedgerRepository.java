package com.hcl.ewallet.repository;

import com.hcl.ewallet.model.TransactionLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLedgerRepository extends JpaRepository<TransactionLedger, Long> {

}
