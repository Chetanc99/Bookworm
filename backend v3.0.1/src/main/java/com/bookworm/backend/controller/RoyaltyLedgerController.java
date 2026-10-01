package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.RoyaltyLedgerResponse;
import com.bookworm.backend.entity.RoyaltyLedger;
import com.bookworm.backend.service.RoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cross-beneficiary royalty views - unlike RoyaltyController (scoped to one
 * beneficiary's own ledger), this looks up every beneficiary's row for a
 * single revenue event, e.g. "how was PurchaseTransaction #42 distributed".
 */
@RestController
@RequestMapping("/api/v1/royalty-ledger")
@RequiredArgsConstructor
@Tag(name = "Royalty Ledger", description = "Cross-beneficiary royalty distribution lookups (Admin CMS)")
public class RoyaltyLedgerController {

    private final RoyaltyService royaltyService;

    @GetMapping("/by-source/{sourceType}/{sourceReferenceId}")
    @Operation(summary = "Full royalty distribution for one revenue event "
            + "(PurchaseItem/RentTransaction/UserLibrary borrow), across every beneficiary paid from it")
    public ResponseEntity<ApiResponse<List<RoyaltyLedgerResponse>>> getDistributionForSource(
            @PathVariable RoyaltyLedger.SourceType sourceType, @PathVariable Long sourceReferenceId) {
        return ResponseEntity.ok(ApiResponse.success(
                royaltyService.getDistributionForSource(sourceType, sourceReferenceId)));
    }
}
