package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.UserShelfResponse;
import com.bookworm.backend.service.UserShelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/shelf")
@RequiredArgsConstructor
@Tag(name = "User Shelf", description = "What a user can currently open/read/play - permanent PURCHASE grants plus active RENT/LIBRARY grants")
public class UserShelfController {

    private final UserShelfService userShelfService;

    @GetMapping
    @Operation(summary = "Get the user's shelf (purchased + actively rented/borrowed items), most recently acquired first")
    public ResponseEntity<ApiResponse<List<UserShelfResponse>>> getShelf(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userShelfService.getShelf(userId)));
    }
}
