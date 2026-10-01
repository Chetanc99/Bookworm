package com.bookworm.backend.service;

/**
 * Central "can this user acquire this product this way right now" gate,
 * shared by cart add/update, Purchase checkout, Rent checkout, and Library
 * borrow, so the rule is defined and enforced exactly once instead of
 * separately re-implemented (and inevitably drifting) in four call sites.
 *
 * Business rules (source of truth = USER_SHELF, the same table every
 * acquisition path already writes to on success):
 *  - A user who has PURCHASED a product owns it permanently (UserShelf
 *    row with source=PURCHASE, expiresAt=null). They can never purchase it
 *    again (no reason to pay twice for the same permanent grant), and can
 *    never rent or borrow it either (they already have unrestricted,
 *    permanent access - a time-boxed grant on top would be strictly worse
 *    and would misrepresent the product as still needing to be acquired).
 *  - A user with a currently-ACTIVE rent (UserShelf source=RENT,
 *    expiresAt in the future) cannot start a second, overlapping rent of the
 *    same product, and cannot borrow it either (same product, same window,
 *    two different "you have time-boxed access" grants would be redundant
 *    and would double-count in royalty terms). They CAN still purchase it -
 *    upgrading from a time-boxed rental to permanent ownership is a normal,
 *    desirable path and must not be blocked by an in-progress rental.
 *  - A user with a currently-ACTIVE library borrow (UserShelf
 *    source=LIBRARY, expiresAt in the future) cannot borrow the same product
 *    again while already holding it, and cannot rent it either (mirrors the
 *    rent case above - it's already a live, time-boxed grant of the same
 *    product). They CAN still purchase it, for the same "upgrade to
 *    permanent ownership" reason as the rent case.
 *  - Expired rent/library grants (expiresAt in the past) impose no
 *    restriction at all - once access has lapsed, the product is exactly as
 *    freely re-acquirable (rent again, borrow again, or purchase) as if it
 *    had never been acquired. This falls out naturally from checking
 *    "expiresAt is in the future," not from any separate cleanup step.
 */
public interface AcquisitionEligibilityService {

    enum AcquisitionType {
        PURCHASE, RENT, BORROW
    }

    /**
     * Throws IllegalArgumentException with a clear, user-facing message if
     * userId is not currently allowed to acquire productId via type, per the
     * rules on this interface. Returns normally (no return value) if allowed -
     * called purely for its side effect of validating, from cart add/update
     * and every checkout/borrow entry point.
     */
    void validate(Long userId, Long productId, AcquisitionType type);
}
