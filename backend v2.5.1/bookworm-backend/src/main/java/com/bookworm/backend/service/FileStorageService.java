package com.bookworm.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Saves the file under the given subfolder (e.g. "covers", "content")
     * and returns the URL path it's servable at (e.g. "/uploads/covers/xyz.jpg").
     */
    String store(MultipartFile file, String subfolder);
}
