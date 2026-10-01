package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.PurchaseTransactionResponse;
import com.bookworm.backend.entity.User;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.service.InvoiceService;
import com.bookworm.backend.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Looks up the transaction/owner (enforcing, via PurchaseService.getById,
 * that purchaseTransactionId actually belongs to userId) and hands off to
 * InvoicePdfRenderer for the actual PDF layout. Rendering itself lives in
 * that separate leaf component - see its javadoc - specifically so
 * PurchaseServiceImpl can reuse the same rendering logic for the checkout
 * receipt email without a circular dependency back on this service.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private final PurchaseService purchaseService;
    private final UserRepository userRepository;
    private final InvoicePdfRenderer renderer;

    @Override
    public byte[] generatePurchaseInvoice(Long userId, Long purchaseTransactionId) {
        PurchaseTransactionResponse tx = purchaseService.getById(userId, purchaseTransactionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "user_id", userId));

        return renderer.render(tx, user.getFullName(), user.getEmail());
    }
}
