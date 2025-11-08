package com.hcl.ewallet.repository;

import com.hcl.ewallet.model.Settlement;
import com.hcl.ewallet.model.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByMerchantMerchantIdAndStatusAndSettlementDateBetween(
        String merchantId,
        SettlementStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
    
    List<Settlement> findByStatusAndSettlementDateBefore(
        SettlementStatus status,
        LocalDateTime date
    );
}