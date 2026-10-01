package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.StakeholderRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.StakeholderResponse;
import com.bookworm.backend.service.StakeholderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stakeholders")
@RequiredArgsConstructor
@Tag(name = "Stakeholders", description = "People/orgs creditable on a product: Author, Publisher, Director, Translator, etc.")
public class StakeholderController {

    private final StakeholderService stakeholderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StakeholderResponse>>> getAll(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<StakeholderResponse> result = activeOnly ? stakeholderService.getAllActive() : stakeholderService.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{stakeholderId}")
    public ResponseEntity<ApiResponse<StakeholderResponse>> getById(@PathVariable Long stakeholderId) {
        return ResponseEntity.ok(ApiResponse.success(stakeholderService.getById(stakeholderId)));
    }

    @PostMapping
    @Operation(summary = "Create a stakeholder (Admin CMS)")
    public ResponseEntity<ApiResponse<StakeholderResponse>> create(@Valid @RequestBody StakeholderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stakeholder created", stakeholderService.create(request)));
    }

    @PutMapping("/{stakeholderId}")
    @Operation(summary = "Update a stakeholder (Admin CMS)")
    public ResponseEntity<ApiResponse<StakeholderResponse>> update(
            @PathVariable Long stakeholderId, @Valid @RequestBody StakeholderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stakeholder updated", stakeholderService.update(stakeholderId, request)));
    }

    @DeleteMapping("/{stakeholderId}")
    @Operation(summary = "Deactivate a stakeholder (soft delete, Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long stakeholderId) {
        stakeholderService.delete(stakeholderId);
        return ResponseEntity.ok(ApiResponse.success("Stakeholder deactivated", null));
    }
}
