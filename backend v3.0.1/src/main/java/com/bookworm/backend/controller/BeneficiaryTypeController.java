package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.BeneficiaryTypeRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.BeneficiaryTypeResponse;
import com.bookworm.backend.service.BeneficiaryTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficiary-types")
@RequiredArgsConstructor
@Tag(name = "Beneficiary Types", description = "Royalty presets (Author/Publisher/Narrator/...) providing a default percentage for new Beneficiaries")
public class BeneficiaryTypeController {

    private final BeneficiaryTypeService beneficiaryTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryTypeResponse>>> getAll(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<BeneficiaryTypeResponse> result =
                activeOnly ? beneficiaryTypeService.getAllActive() : beneficiaryTypeService.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{beneficiaryTypeId}")
    public ResponseEntity<ApiResponse<BeneficiaryTypeResponse>> getById(@PathVariable Long beneficiaryTypeId) {
        return ResponseEntity.ok(ApiResponse.success(beneficiaryTypeService.getById(beneficiaryTypeId)));
    }

    @PostMapping
    @Operation(summary = "Create a beneficiary type preset (Admin CMS)")
    public ResponseEntity<ApiResponse<BeneficiaryTypeResponse>> create(@Valid @RequestBody BeneficiaryTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiary type created", beneficiaryTypeService.create(request)));
    }

    @PutMapping("/{beneficiaryTypeId}")
    @Operation(summary = "Update a beneficiary type preset (Admin CMS)")
    public ResponseEntity<ApiResponse<BeneficiaryTypeResponse>> update(
            @PathVariable Long beneficiaryTypeId, @Valid @RequestBody BeneficiaryTypeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Beneficiary type updated", beneficiaryTypeService.update(beneficiaryTypeId, request)));
    }

    @DeleteMapping("/{beneficiaryTypeId}")
    @Operation(summary = "Deactivate a beneficiary type (soft delete, Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long beneficiaryTypeId) {
        beneficiaryTypeService.delete(beneficiaryTypeId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary type deactivated", null));
    }
}
