package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.AuditLogResponse;
import com.bookworm.backend.dto.response.PageResponse;
import com.bookworm.backend.entity.AuditLog;
import com.bookworm.backend.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only view over AUDIT_LOGS (see AuditServiceImpl for how rows get
 * written). No service layer needed for a straight paginated read - kept
 * thin, same as other simple list endpoints in this codebase.
 */
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin - Audit Logs", description = "Trail of admin-initiated changes (ADMIN only)")
public class AuditLogController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "List audit log entries, most recent first")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        Page<AuditLogResponse> result = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    private AuditLogResponse toResponse(AuditLog entity) {
        return AuditLogResponse.builder()
                .auditLogId(entity.getAuditLogId())
                .actorUserId(entity.getActorUserId())
                .actorEmail(entity.getActorEmail())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
