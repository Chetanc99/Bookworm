package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.BorrowRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.UserLibraryPackageResponse;
import com.bookworm.backend.dto.response.UserLibraryResponse;
import com.bookworm.backend.service.UserLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/library")
@RequiredArgsConstructor
@Tag(name = "User Library", description = "Subscribe to a library package, then borrow/return eligible products against it")
public class UserLibraryController {

    private final UserLibraryService service;

    @PostMapping("/subscriptions/{libraryPackageId}")
    @Operation(summary = "Subscribe to a library package (rejected if an ACTIVE subscription already exists)")
    public ResponseEntity<ApiResponse<UserLibraryPackageResponse>> subscribe(
            @PathVariable Long userId, @PathVariable Long libraryPackageId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscribed", service.subscribe(userId, libraryPackageId)));
    }

    @GetMapping("/subscriptions/active")
    @Operation(summary = "Get the user's current active subscription")
    public ResponseEntity<ApiResponse<UserLibraryPackageResponse>> getActiveSubscription(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(service.getActiveSubscription(userId)));
    }

    @PostMapping("/borrows")
    @Operation(summary = "Borrow a library-eligible product against the active subscription")
    public ResponseEntity<ApiResponse<UserLibraryResponse>> borrow(
            @PathVariable Long userId, @Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item borrowed", service.borrow(userId, request)));
    }

    @PostMapping("/borrows/{userLibraryId}/return")
    @Operation(summary = "Return a borrowed item before its due date")
    public ResponseEntity<ApiResponse<UserLibraryResponse>> returnItem(
            @PathVariable Long userId, @PathVariable Long userLibraryId) {
        return ResponseEntity.ok(ApiResponse.success("Item returned", service.returnItem(userId, userLibraryId)));
    }

    @GetMapping("/borrows/active")
    @Operation(summary = "List currently borrowed (not yet returned) items")
    public ResponseEntity<ApiResponse<List<UserLibraryResponse>>> getActiveBorrows(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(service.getActiveBorrows(userId)));
    }
}
