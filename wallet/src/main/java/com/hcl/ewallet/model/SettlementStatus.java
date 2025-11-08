package com.hcl.ewallet.model;

public enum SettlementStatus {
    PENDING,         // Settlement is created but not yet processed
    PROCESSING,      // Settlement is being processed
    COMPLETED,       // Settlement has been successfully processed
    FAILED,         // Settlement failed during processing
}