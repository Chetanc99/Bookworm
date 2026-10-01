package com.bookworm.backend.service.impl;

import com.bookworm.backend.entity.AuditLog;
import com.bookworm.backend.repository.AuditLogRepository;
import com.bookworm.backend.security.UserPrincipal;
import com.bookworm.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    // REQUIRES_NEW so an audit-write failure (or the caller's transaction
    // later rolling back for an unrelated reason) never affects, and is never
    // affected by, the surrounding business transaction it's being called from.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, String details) {
        try {
            Long actorUserId = null;
            String actorEmail = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                actorUserId = principal.getUserId();
                actorEmail = principal.getUsername();
            }

            auditLogRepository.save(AuditLog.builder()
                    .actorUserId(actorUserId)
                    .actorEmail(actorEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .build());
        } catch (Exception ex) {
            // Auditing must never break the actual business operation it's attached to.
            log.warn("Failed to write audit log entry for action {}: {}", action, ex.getMessage());
        }
    }
}
