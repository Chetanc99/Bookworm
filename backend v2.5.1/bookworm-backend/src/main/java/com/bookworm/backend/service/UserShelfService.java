package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.UserShelfResponse;

import java.util.List;

public interface UserShelfService {

    /**
     * Everything the user currently has shelf access to (purchased
     * permanently, or actively rented/borrowed with a live expiresAt) -
     * most recently acquired first. Expired rows are pruned by
     * ExpiryCleanupScheduler, so this is a plain lookup, not a filter.
     */
    List<UserShelfResponse> getShelf(Long userId);
}
