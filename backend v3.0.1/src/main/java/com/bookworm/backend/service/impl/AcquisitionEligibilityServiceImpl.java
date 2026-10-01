package com.bookworm.backend.service.impl;

import com.bookworm.backend.entity.UserShelf;
import com.bookworm.backend.repository.UserShelfRepository;
import com.bookworm.backend.service.AcquisitionEligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcquisitionEligibilityServiceImpl implements AcquisitionEligibilityService {

    private final UserShelfRepository userShelfRepository;

    @Override
    public void validate(Long userId, Long productId, AcquisitionType type) {
        boolean alreadyOwned = userShelfRepository
                .existsByUserIdAndProduct_ProductIdAndSource(userId, productId, UserShelf.Source.PURCHASE);
        if (alreadyOwned) {
            // Permanent ownership blocks every acquisition path, including a
            // repeat purchase - see interface javadoc.
            throw new IllegalArgumentException(type == AcquisitionType.PURCHASE
                    ? "You already own this product"
                    : "You already own this product - no need to rent or borrow it");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean activeRent = userShelfRepository.existsByUserIdAndProduct_ProductIdAndSourceAndExpiresAtAfter(
                userId, productId, UserShelf.Source.RENT, now);
        boolean activeBorrow = userShelfRepository.existsByUserIdAndProduct_ProductIdAndSourceAndExpiresAtAfter(
                userId, productId, UserShelf.Source.LIBRARY, now);

        switch (type) {
            case PURCHASE -> {
                // Upgrading an active rent/borrow to full ownership is always allowed.
            }
            case RENT -> {
                if (activeRent) {
                    throw new IllegalArgumentException("You already have an active rental for this product");
                }
                if (activeBorrow) {
                    throw new IllegalArgumentException(
                            "You currently have this product borrowed from the library - return it or wait for it to expire before renting it");
                }
            }
            case BORROW -> {
                if (activeBorrow) {
                    throw new IllegalArgumentException("You already have this product borrowed");
                }
                if (activeRent) {
                    throw new IllegalArgumentException(
                            "You currently have this product actively rented - it can't also be borrowed at the same time");
                }
            }
        }
    }
}
