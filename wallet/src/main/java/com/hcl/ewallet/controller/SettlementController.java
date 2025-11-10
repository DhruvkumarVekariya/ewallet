package com.hcl.ewallet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hcl.ewallet.model.Settlement;
import com.hcl.ewallet.service.SettlementService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/settlements")
public class SettlementController {
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementController.class);
    
    @Autowired
    private SettlementService settlementService;

    @PostMapping("/initiate/{merchantId}")
    public ResponseEntity<Settlement> initiateSettlement(@PathVariable String merchantId) {
        logger.info("Initiating settlement for merchant: {}", merchantId);
        try {
            Settlement settlement = settlementService.initiateSettlement(merchantId);
            logger.info("Settlement initiated successfully for merchant: {}, settlement ID: {}", merchantId, settlement.getId());
            return ResponseEntity.ok(settlement);
        } catch (Exception e) {
            logger.error("Error initiating settlement for merchant: {}", merchantId, e);
            throw e;
        }
    }
 

    @PostMapping("/process-pending")
    public ResponseEntity<Void> processPendingSettlements() {
        logger.info("Processing pending settlements");
        try {
            settlementService.processPendingSettlements();
            logger.info("Successfully processed pending settlements");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error processing pending settlements", e);
            throw e;
        }
    }

    @PostMapping("/day-end")
    public ResponseEntity<Void> triggerDayEndSettlements() {
        logger.info("Triggering day-end settlements");
        try {
            settlementService.processDayEndSettlements();
            logger.info("Successfully processed day-end settlements");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error processing day-end settlements", e);
            throw e;
            }
    }
}