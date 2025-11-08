package com.hcl.ewallet.repository;

import com.hcl.ewallet.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Merchant findByMerchantId(String merchantId);
}