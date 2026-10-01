package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.LanguageRequest;
import com.bookworm.backend.dto.response.LanguageResponse;
import com.bookworm.backend.entity.Language;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.LanguageMapper;
import com.bookworm.backend.repository.LanguageRepository;
import com.bookworm.backend.service.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    @Override
    public List<LanguageResponse> getAllActive() {
        return languageRepository.findByIsActiveTrue().stream().map(languageMapper::toResponse).toList();
    }

    @Override
    public List<LanguageResponse> getAll() {
        return languageRepository.findAll().stream().map(languageMapper::toResponse).toList();
    }

    @Override
    public LanguageResponse getById(Long languageId) {
        return languageMapper.toResponse(findEntityOrThrow(languageId));
    }

    @Override
    @Transactional
    public LanguageResponse create(LanguageRequest request) {
        if (languageRepository.existsByLanguageNameIgnoreCase(request.getLanguageName())) {
            throw new DuplicateResourceException("Language '" + request.getLanguageName() + "' already exists");
        }
        Language language = Language.builder()
                .languageName(request.getLanguageName())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return languageMapper.toResponse(languageRepository.save(language));
    }

    @Override
    @Transactional
    public LanguageResponse update(Long languageId, LanguageRequest request) {
        Language language = findEntityOrThrow(languageId);
        if (!language.getLanguageName().equalsIgnoreCase(request.getLanguageName())
                && languageRepository.existsByLanguageNameIgnoreCase(request.getLanguageName())) {
            throw new DuplicateResourceException("Language '" + request.getLanguageName() + "' already exists");
        }
        language.setLanguageName(request.getLanguageName());
        if (request.getIsActive() != null) language.setIsActive(request.getIsActive());
        return languageMapper.toResponse(languageRepository.save(language));
    }

    @Override
    @Transactional
    public void delete(Long languageId) {
        Language language = findEntityOrThrow(languageId);
        // Soft delete - PRODUCTS.language_id is NOT NULL with ON DELETE RESTRICT.
        language.setIsActive(false);
        languageRepository.save(language);
    }

    private Language findEntityOrThrow(Long languageId) {
        return languageRepository.findById(languageId)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "language_id", languageId));
    }
}
