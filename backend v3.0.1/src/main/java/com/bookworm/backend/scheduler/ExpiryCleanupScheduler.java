package com.bookworm.backend.scheduler;

import com.bookworm.backend.entity.RentTransaction;
import com.bookworm.backend.entity.UserLibrary;
import com.bookworm.backend.entity.UserLibraryPackage;
import com.bookworm.backend.entity.UserShelf;
import com.bookworm.backend.repository.RentTransactionRepository;
import com.bookworm.backend.repository.UserLibraryPackageRepository;
import com.bookworm.backend.repository.UserLibraryRepository;
import com.bookworm.backend.repository.UserShelfRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sweeps everything time-bounded and flips it to its terminal state once
 * its window has passed. Runs as four independent steps rather than one
 * transaction, so a failure in one (e.g. subscription cascade) doesn't
 * roll back progress already made on the others - each step logs and
 * moves on.
 *
 * Order matters for the library side: subscriptions are expired (and
 * their still-BORROWED loans force-expired) before the standalone
 * due-date sweep runs, so a loan is never counted/logged twice - once
 * under the cascade and once under its own due-date check.
 *
 * Shelf pruning runs last, after every source table it depends on
 * (RentTransaction, UserLibrary) has already been brought up to date,
 * since it just deletes rows by expiresAt with no cross-check against
 * those tables' status.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiryCleanupScheduler {

    private final RentTransactionRepository rentTransactionRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final UserLibraryPackageRepository userLibraryPackageRepository;
    private final UserShelfRepository userShelfRepository;

    // Every 15 minutes; fixedDelay (not fixedRate) so a slow run never overlaps the next one.
    @Scheduled(fixedDelayString = "${bookworm.expiry-cleanup.fixed-delay-ms:900000}")
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        int rents = expireRents(now);
        int subscriptions = expireSubscriptions(now);
        int borrows = expireOverdueBorrows(now);
        int shelfRows = pruneShelf(now);

        if (rents + subscriptions + borrows + shelfRows > 0) {
            log.info("Expiry cleanup: {} rent(s), {} subscription(s), {} borrow(s) expired; {} shelf row(s) pruned",
                    rents, subscriptions, borrows, shelfRows);
        }
    }

    @Transactional
    public int expireRents(LocalDateTime now) {
        List<RentTransaction> due = rentTransactionRepository
                .findByStatusAndEndDateBefore(RentTransaction.Status.ACTIVE, now);
        due.forEach(rent -> rent.setStatus(RentTransaction.Status.EXPIRED));
        rentTransactionRepository.saveAll(due);
        return due.size();
    }

    @Transactional
    public int expireSubscriptions(LocalDateTime now) {
        List<UserLibraryPackage> due = userLibraryPackageRepository
                .findByStatusAndEndDateBefore(UserLibraryPackage.Status.ACTIVE, now);

        for (UserLibraryPackage subscription : due) {
            subscription.setStatus(UserLibraryPackage.Status.EXPIRED);

            // A subscription ending force-expires any loan still open under it,
            // even one whose own due_date hasn't arrived yet.
            List<UserLibrary> openBorrows = userLibraryRepository
                    .findByUserLibraryPackage_UserLibraryPackageIdAndStatus(
                            subscription.getUserLibraryPackageId(), UserLibrary.Status.BORROWED);
            openBorrows.forEach(borrow -> borrow.setStatus(UserLibrary.Status.EXPIRED));
            userLibraryRepository.saveAll(openBorrows);
        }
        userLibraryPackageRepository.saveAll(due);
        return due.size();
    }

    @Transactional
    public int expireOverdueBorrows(LocalDateTime now) {
        // Loans whose own due_date has passed but whose parent subscription is
        // still ACTIVE - not touched by the cascade above.
        List<UserLibrary> due = userLibraryRepository
                .findByStatusAndDueDateBefore(UserLibrary.Status.BORROWED, now);
        due.forEach(borrow -> borrow.setStatus(UserLibrary.Status.EXPIRED));
        userLibraryRepository.saveAll(due);
        return due.size();
    }

    @Transactional
    public int pruneShelf(LocalDateTime now) {
        List<UserShelf> expired = userShelfRepository.findByExpiresAtBefore(now);
        userShelfRepository.deleteAll(expired);
        return expired.size();
    }
}
