package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.LanguageRequest;
import com.bookworm.backend.dto.response.LanguageResponse;

import java.util.List;

public interface LanguageService {
    List<LanguageResponse> getAllActive();
    List<LanguageResponse> getAll();
    LanguageResponse getById(Long languageId);
    LanguageResponse create(LanguageRequest request);
    LanguageResponse update(Long languageId, LanguageRequest request);
    void delete(Long languageId);
}
