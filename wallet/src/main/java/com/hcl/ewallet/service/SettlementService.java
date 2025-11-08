package com.hcl.ewallet.service;

import com.hcl.ewallet.model.Settlement;


public interface SettlementService {
    
    Settlement initiateSettlement(String merchantId);
    
    void processPendingSettlements();

    void processDayEndSettlements();

}