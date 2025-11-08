package com.hcl.ewallet.controller;

import com.hcl.ewallet.model.Settlement;
import com.hcl.ewallet.service.SettlementService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    
    @Autowired
    private SettlementService settlementService;

    @PostMapping("/initiate/{merchantId}")
    public ResponseEntity<Settlement> initiateSettlement(@PathVariable String merchantId) {
        Settlement settlement = settlementService.initiateSettlement(merchantId);
        return ResponseEntity.ok(settlement);
    }
 

    @PostMapping("/process-pending")
    public ResponseEntity<Void> processPendingSettlements() {
        settlementService.processPendingSettlements();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/day-end")
    public ResponseEntity<Void> triggerDayEndSettlements() {
        settlementService.processDayEndSettlements();
        return ResponseEntity.ok().build();
    }
}