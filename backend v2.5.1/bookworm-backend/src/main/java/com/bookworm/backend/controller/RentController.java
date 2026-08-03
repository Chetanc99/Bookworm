package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.PageResponse;
import com.bookworm.backend.dto.response.RentTransactionResponse;
import com.bookworm.backend.service.RentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/rentals")
@RequiredArgsConstructor
@Tag(name = "Rent", description = "Checkout of RENT-intent cart items; grants time-limited shelf access")
public class RentController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RentService rentService;

    @PostMapping
    @Operation(summary = "Check out all RENT-intent items currently in the user's cart")
    public ResponseEntity<ApiResponse<List<RentTransactionResponse>>> checkout(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rental(s) started", rentService.checkout(userId)));
    }

    @GetMapping
    @Operation(summary = "Rental history for the user, most recent first")
    public ResponseEntity<ApiResponse<PageResponse<RentTransactionResponse>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        Page<RentTransactionResponse> result = rentService.getHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/{rentTransactionId}")
    @Operation(summary = "Get a single rent transaction by id")
    public ResponseEntity<ApiResponse<RentTransactionResponse>> getById(
            @PathVariable Long userId, @PathVariable Long rentTransactionId) {
        return ResponseEntity.ok(ApiResponse.success(rentService.getById(userId, rentTransactionId)));
    }
}
