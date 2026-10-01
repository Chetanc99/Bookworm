package com.bookworm.backend.service;

/**
 * Fire-and-forget audit trail for admin-initiated changes. Resolves the
 * acting user from the current SecurityContext internally, so call sites
 * don't need to thread an actor id/email through every method signature -
 * keeps this purely additive against existing service/controller contracts.
 */
public interface AuditService {

    /**
     * @param action     short verb code, e.g. "PRODUCT_UPDATE" (see AuditLog for the set in use)
     * @param entityType e.g. "PRODUCT", "USER" - nullable
     * @param entityId   nullable
     * @param details    free-text context, nullable
     */
    void log(String action, String entityType, Long entityId, String details);
}
