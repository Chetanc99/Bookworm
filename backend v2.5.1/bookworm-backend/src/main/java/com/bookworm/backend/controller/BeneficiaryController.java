package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.BeneficiaryRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.BeneficiaryResponse;
import com.bookworm.backend.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiaries", description = "Royalty recipients tied to a product via a percentage split (distinct from Stakeholders/credits)")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getAll(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<BeneficiaryResponse> result = activeOnly ? beneficiaryService.getAllActive() : beneficiaryService.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{beneficiaryId}")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> getById(@PathVariable Long beneficiaryId) {
        return ResponseEntity.ok(ApiResponse.success(beneficiaryService.getById(beneficiaryId)));
    }

    @PostMapping
    @Operation(summary = "Create a beneficiary (Admin CMS)")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> create(@Valid @RequestBody BeneficiaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiary created", beneficiaryService.create(request)));
    }

    @PutMapping("/{beneficiaryId}")
    @Operation(summary = "Update a beneficiary (Admin CMS)")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> update(
            @PathVariable Long beneficiaryId, @Valid @RequestBody BeneficiaryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Beneficiary updated", beneficiaryService.update(beneficiaryId, request)));
    }

    @DeleteMapping("/{beneficiaryId}")
    @Operation(summary = "Deactivate a beneficiary (soft delete, Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long beneficiaryId) {
        beneficiaryService.delete(beneficiaryId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deactivated", null));
    }
}
