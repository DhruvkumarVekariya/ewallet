package com.hcl.ewallet.model;

public enum WalletTransactionType {
    LOAD_MONEY,           // User loading money into their wallet
    TRANSFER_TO_MERCHANT, // User transferring money to merchant wallet
    MERCHANT_SETTLEMENT,  // Merchant settlement (from merchant wallet to external account)
    REFUND                // Money refunded back to wallet
}
