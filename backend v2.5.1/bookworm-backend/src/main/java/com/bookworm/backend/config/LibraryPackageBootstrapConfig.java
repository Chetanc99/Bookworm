package com.bookworm.backend.config;

import com.bookworm.backend.entity.LibraryPackage;
import com.bookworm.backend.repository.LibraryPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the three standard library subscription tiers on startup, same
 * CommandLineRunner + existence-check pattern as AdminBootstrapConfig's
 * admin-account seed. Idempotent - existsByPackageNameIgnoreCase skips any
 * tier that's already there (whether from a previous boot or an admin
 * having edited/recreated it), so this never creates duplicates and never
 * overwrites an admin's edits to an existing package's price/limits.
 *
 * "Only lendable books" isn't a per-package field - every package draws from
 * the same catalog, and UserLibraryServiceImpl.borrow() already rejects any
 * product where isLibraryEligible=false regardless of which package the
 * borrower is subscribed to. Seeding these three packages doesn't need (or
 * get) any different enforcement of that rule.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class LibraryPackageBootstrapConfig {

    @Bean
    public CommandLineRunner seedLibraryPackages(LibraryPackageRepository libraryPackageRepository) {
        return args -> {
            List<LibraryPackage> defaults = List.of(
                    LibraryPackage.builder()
                            .packageName("Weekly")
                            .description("7-day library access - up to 3 books borrowed at once")
                            .price(new BigDecimal("99.00"))
                            .durationDays(7)
                            .maxConcurrentBorrows(3)
                            .isActive(true)
                            .build(),
                    LibraryPackage.builder()
                            .packageName("Monthly")
                            .description("30-day library access - up to 10 books borrowed at once")
                            .price(new BigDecimal("299.00"))
                            .durationDays(30)
                            .maxConcurrentBorrows(10)
                            .isActive(true)
                            .build(),
                    LibraryPackage.builder()
                            .packageName("Quarterly")
                            .description("90-day library access - up to 30 books borrowed at once")
                            .price(new BigDecimal("799.00"))
                            .durationDays(90)
                            .maxConcurrentBorrows(30)
                            .isActive(true)
                            .build()
            );

            for (LibraryPackage pkg : defaults) {
                if (libraryPackageRepository.existsByPackageNameIgnoreCase(pkg.getPackageName())) {
                    continue;
                }
                libraryPackageRepository.save(pkg);
                log.info("Seeded default library package: {} ({} days, max {} concurrent borrows)",
                        pkg.getPackageName(), pkg.getDurationDays(), pkg.getMaxConcurrentBorrows());
            }
        };
    }
}
