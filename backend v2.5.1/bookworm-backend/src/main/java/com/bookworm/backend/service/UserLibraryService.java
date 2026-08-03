package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.BorrowRequest;
import com.bookworm.backend.dto.response.UserLibraryPackageResponse;
import com.bookworm.backend.dto.response.UserLibraryResponse;

import java.util.List;

public interface UserLibraryService {

    /** Starts a new subscription. Rejects if the user already has an ACTIVE one. */
    UserLibraryPackageResponse subscribe(Long userId, Long libraryPackageId);

    UserLibraryPackageResponse getActiveSubscription(Long userId);

    /**
     * Borrows a library-eligible product against the user's active
     * subscription. Rejected (not deferred) if: no active subscription,
     * product not library-eligible/available, concurrent-borrow cap
     * reached, or borrowDays would extend past the subscription's end_date.
     */
    UserLibraryResponse borrow(Long userId, BorrowRequest request);

    UserLibraryResponse returnItem(Long userId, Long userLibraryId);

    List<UserLibraryResponse> getActiveBorrows(Long userId);
}
