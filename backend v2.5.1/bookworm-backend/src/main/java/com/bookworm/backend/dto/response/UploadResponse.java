package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UploadResponse {
    // Servable path, e.g. "/uploads/covers/<uuid>.jpg" - store this directly
    // in ProductRequest.coverImage / .filePath.
    private String url;
}
