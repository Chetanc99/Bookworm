package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.PurchaseTransactionResponse;
import com.bookworm.backend.entity.*;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.PurchaseMapper;
import com.bookworm.backend.repository.*;
import com.bookworm.backend.service.AcquisitionEligibilityService;
import com.bookworm.backend.service.MailService;
import com.bookworm.backend.service.PurchaseService;
import com.bookworm.backend.service.RoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Ties Cart, UserShelf and LoyaltyLedger together for the purchase path.
 * No real payment gateway (scope: mocked) - checkout is synchronous and
 * always succeeds once validated; there's no async payment-confirmation
 * step to reconcile later.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    // 1 loyalty point per whole currency unit spent - simplest rule that satisfies
    // "loyalty earn" scope without inventing a points-tier system nobody asked for.
    private static final BigDecimal POINTS_PER_CURRENCY_UNIT = BigDecimal.ONE;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final UserShelfRepository userShelfRepository;
    private final LoyaltyLedgerRepository loyaltyLedgerRepository;
    private final UserRepository userRepository;
    private final RoyaltyService royaltyService;
    private final AcquisitionEligibilityService acquisitionEligibilityService;
    private final MailService mailService;
    private final InvoicePdfRenderer invoicePdfRenderer;
    private final PurchaseMapper mapper;

    @Override
    @Transactional
    public PurchaseTransactionResponse checkout(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty - nothing to check out"));

        List<CartItem> purchaseItems = cartItemRepository.findByCart_CartId(cart.getCartId()).stream()
                .filter(item -> item.getIntent() == CartItem.Intent.PURCHASE)
                .toList();

        if (purchaseItems.isEmpty()) {
            throw new IllegalArgumentException("No PURCHASE-intent items in cart to check out");
        }

        // Re-validate availability at checkout time too - a product could have gone
        // unavailable after being added to the cart. Also re-validate acquisition
        // eligibility here (not just at add-to-cart) - a user could have purchased,
        // rented, or borrowed the same product through a different flow in the
        // time between adding it to the cart and checking out.
        for (CartItem item : purchaseItems) {
            if (!Boolean.TRUE.equals(item.getProduct().getIsAvailable())) {
                throw new IllegalArgumentException(
                        "Product '" + item.getProduct().getTitle() + "' is no longer available");
            }
            acquisitionEligibilityService.validate(userId, item.getProduct().getProductId(),
                    AcquisitionEligibilityService.AcquisitionType.PURCHASE);
        }

        BigDecimal totalAmount = purchaseItems.stream()
                .map(item -> item.getProduct().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseTransaction transaction = purchaseTransactionRepository.save(
                PurchaseTransaction.builder()
                        .userId(userId)
                        .totalAmount(totalAmount)
                        .status(PurchaseTransaction.Status.COMPLETED)
                        .build());

        for (CartItem item : purchaseItems) {
            Product product = item.getProduct();

            purchaseItemRepository.save(PurchaseItem.builder()
                    .purchaseTransaction(transaction)
                    .product(product)
                    .unitPrice(product.getPrice())
                    .build());

            royaltyService.recordForProduct(product, product.getPrice(),
                    com.bookworm.backend.entity.RoyaltyLedger.SourceType.PURCHASE,
                    transaction.getPurchaseTransactionId());

            // Permanent shelf grant - expiresAt stays null for purchases.
            if (!userShelfRepository.existsByUserIdAndProduct_ProductIdAndSource(
                    userId, product.getProductId(), UserShelf.Source.PURCHASE)) {
                userShelfRepository.save(UserShelf.builder()
                        .userId(userId)
                        .product(product)
                        .source(UserShelf.Source.PURCHASE)
                        .sourceReferenceId(transaction.getPurchaseTransactionId())
                        .expiresAt(null)
                        .build());
            }
        }

        int pointsEarned = totalAmount
                .multiply(POINTS_PER_CURRENCY_UNIT)
                .setScale(0, RoundingMode.DOWN)
                .intValueExact();

        if (pointsEarned > 0) {
            loyaltyLedgerRepository.save(LoyaltyLedger.builder()
                    .userId(userId)
                    .entryType(LoyaltyLedger.EntryType.EARN)
                    .points(pointsEarned)
                    .reason("Purchase")
                    .referenceType("PURCHASE_TRANSACTION")
                    .referenceId(transaction.getPurchaseTransactionId())
                    .build());
        }

        // Only the checked-out items leave the cart - any RENT-intent items stay put
        // for a separate Rent-flow checkout.
        purchaseItems.forEach(cartItemRepository::delete);

        List<PurchaseItem> savedItems =
                purchaseItemRepository.findByPurchaseTransaction_PurchaseTransactionId(transaction.getPurchaseTransactionId());

        // Built once and reused for both the email attachment and the return value -
        // avoids re-querying/re-mapping the same transaction twice.
        PurchaseTransactionResponse response = mapper.toResponse(transaction, savedItems, pointsEarned);

        sendReceiptEmail(userId, transaction, purchaseItems, totalAmount, pointsEarned, response);

        return response;
    }

    // Best-effort only - a mail failure must never roll back a completed
    // purchase (same reasoning as AuthServiceImpl.register's verification
    // email). Logged at warn so a broken mail config is still visible.
    private void sendReceiptEmail(Long userId, PurchaseTransaction transaction, List<CartItem> purchaseItems,
                                   BigDecimal totalAmount, int pointsEarned, PurchaseTransactionResponse response) {
        try {
            userRepository.findById(userId).ifPresent(user -> {
                StringBuilder body = new StringBuilder();
                body.append("Thanks for your purchase, ").append(user.getFullName()).append("!\n\n");
                body.append("Order #").append(transaction.getPurchaseTransactionId()).append("\n");
                for (CartItem item : purchaseItems) {
                    body.append(" - ").append(item.getProduct().getTitle())
                            .append(" (₹").append(item.getProduct().getPrice()).append(")\n");
                }
                body.append("\nTotal: ₹").append(totalAmount);
                if (pointsEarned > 0) {
                    body.append("\nLoyalty points earned: ").append(pointsEarned);
                }
                body.append("\n\nYour purchases are available on your Bookworm shelf.");
                body.append("\nYour invoice is attached to this email as a PDF.");

                String subject = "Your Bookworm order #" + transaction.getPurchaseTransactionId();

                // Reuses the same renderer InvoiceServiceImpl uses for the on-demand
                // download endpoint - no separate PDF-generation code path, and no
                // second DB round-trip since `response` was already built above.
                byte[] invoicePdf = invoicePdfRenderer.render(response, user.getFullName(), user.getEmail());
                mailService.sendWithAttachment(user.getEmail(), subject, body.toString(),
                        "bookworm-invoice-" + transaction.getPurchaseTransactionId() + ".pdf",
                        invoicePdf, "application/pdf");
            });
        } catch (Exception ex) {
            log.warn("Failed to send purchase receipt email for transaction {}: {}",
                    transaction.getPurchaseTransactionId(), ex.getMessage());
        }
    }

    @Override
    public Page<PurchaseTransactionResponse> getHistory(Long userId, Pageable pageable) {
        return purchaseTransactionRepository.findByUserId(userId, pageable)
                .map(tx -> mapper.toResponse(
                        tx,
                        purchaseItemRepository.findByPurchaseTransaction_PurchaseTransactionId(tx.getPurchaseTransactionId()),
                        pointsEarnedFor(tx.getPurchaseTransactionId())));
    }

    @Override
    public PurchaseTransactionResponse getById(Long userId, Long purchaseTransactionId) {
        PurchaseTransaction transaction = purchaseTransactionRepository.findById(purchaseTransactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PurchaseTransaction", "purchase_transaction_id", purchaseTransactionId));
        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "PurchaseTransaction", "purchase_transaction_id", purchaseTransactionId);
        }
        List<PurchaseItem> items =
                purchaseItemRepository.findByPurchaseTransaction_PurchaseTransactionId(purchaseTransactionId);
        return mapper.toResponse(transaction, items, pointsEarnedFor(purchaseTransactionId));
    }

    private int pointsEarnedFor(Long purchaseTransactionId) {
        return loyaltyLedgerRepository
                .findByReferenceTypeAndReferenceId("PURCHASE_TRANSACTION", purchaseTransactionId)
                .map(LoyaltyLedger::getPoints)
                .orElse(0);
    }
}
