package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.CreditTypeRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.CreditTypeResponse;
import com.bookworm.backend.service.CreditTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credit-types")
@RequiredArgsConstructor
@Tag(name = "Credit Types", description = "Attribution roles: Translator, Illustrator, Narrator, Music Director, etc.")
public class CreditTypeController {

    private final CreditTypeService creditTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CreditTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(creditTypeService.getAll()));
    }

    @GetMapping("/{creditTypeId}")
    public ResponseEntity<ApiResponse<CreditTypeResponse>> getById(@PathVariable Long creditTypeId) {
        return ResponseEntity.ok(ApiResponse.success(creditTypeService.getById(creditTypeId)));
    }

    @PostMapping
    @Operation(summary = "Create a credit type (Admin CMS)")
    public ResponseEntity<ApiResponse<CreditTypeResponse>> create(@Valid @RequestBody CreditTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Credit type created", creditTypeService.create(request)));
    }

    @PutMapping("/{creditTypeId}")
    @Operation(summary = "Rename a credit type (Admin CMS)")
    public ResponseEntity<ApiResponse<CreditTypeResponse>> update(
            @PathVariable Long creditTypeId, @Valid @RequestBody CreditTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Credit type updated", creditTypeService.update(creditTypeId, request)));
    }

    @DeleteMapping("/{creditTypeId}")
    @Operation(summary = "Delete a credit type (Admin CMS) - blocked if any product still uses it")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long creditTypeId) {
        creditTypeService.delete(creditTypeId);
        return ResponseEntity.ok(ApiResponse.success("Credit type deleted", null));
    }
}
