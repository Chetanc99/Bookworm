package com.bookworm.backend.service;

public interface InvoiceService {

    /** Renders a simple PDF invoice for a completed purchase transaction, scoped to its owner. */
    byte[] generatePurchaseInvoice(Long userId, Long purchaseTransactionId);
}
