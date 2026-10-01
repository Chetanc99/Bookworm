package com.bookworm.backend.config;

import com.bookworm.backend.entity.Product;
import com.bookworm.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * One-time startup backfill for a data bug, not a feature: before
 * ProductServiceImpl.validateRentFields required rentRate whenever
 * isLibraryEligible=true, any product created as library-eligible-but-not-
 * rentable (which is the default for both bulk-import paths - see
 * ProductBulkImportServiceImpl) was saved with rentRate=null. Because
 * RoyaltyServiceImpl values a library borrow as rentRate x borrowDays,
 * UserLibraryServiceImpl.borrow() was writing real RoyaltyLedger rows on every
 * borrow of those products, just always with royaltyAmount=0.00 - "royalty
 * recorded" in the ledger but functionally invisible, which is the bug that
 * was reported as "borrowing doesn't create royalty ledger entries."
 *
 * New products can no longer be saved in this broken state (the validation
 * closes the gap going forward), but existing rows already in the database
 * need a rate before a borrow of them will earn anything. This runs once at
 * startup, idempotently (only touches rows that still have rentRate=null AND
 * isLibraryEligible=true), and derives the same conservative default
 * (5% of price per day) the bulk importer now uses for new rows, so an admin
 * can immediately override it per-product afterward via Manage Catalog if a
 * different rate is more appropriate.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class LibraryRentRateBackfillConfig {

    private static final BigDecimal DEFAULT_DAILY_RATE_PERCENT = new BigDecimal("0.05");

    @Bean
    public CommandLineRunner backfillLibraryRentRates(ProductRepository productRepository) {
        return args -> {
            List<Product> broken = productRepository.findAll().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsLibraryEligible()) && p.getRentRate() == null)
                    .toList();

            if (broken.isEmpty()) {
                return;
            }

            backfill(productRepository, broken);
            log.info("Backfilled rentRate on {} library-eligible product(s) that had none "
                    + "(previously caused $0.00 royalty on every borrow)", broken.size());
        };
    }

    @Transactional
    void backfill(ProductRepository productRepository, List<Product> broken) {
        for (Product product : broken) {
            BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            product.setRentRate(price.multiply(DEFAULT_DAILY_RATE_PERCENT).setScale(2, RoundingMode.HALF_UP));
            productRepository.save(product);
        }
    }
}
