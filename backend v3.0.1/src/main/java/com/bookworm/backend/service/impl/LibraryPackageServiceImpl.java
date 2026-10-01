package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.LibraryPackageRequest;
import com.bookworm.backend.dto.response.LibraryPackageResponse;
import com.bookworm.backend.entity.LibraryPackage;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.LibraryPackageMapper;
import com.bookworm.backend.repository.LibraryPackageRepository;
import com.bookworm.backend.service.LibraryPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryPackageServiceImpl implements LibraryPackageService {

    private final LibraryPackageRepository repository;
    private final LibraryPackageMapper mapper;

    @Override
    public List<LibraryPackageResponse> getAllActive() {
        return repository.findByIsActiveTrue().stream().map(mapper::toResponse).toList();
    }

    @Override
    public LibraryPackageResponse getById(Long libraryPackageId) {
        return mapper.toResponse(findEntityOrThrow(libraryPackageId));
    }

    @Override
    @Transactional
    public LibraryPackageResponse create(LibraryPackageRequest request) {
        LibraryPackage entity = LibraryPackage.builder()
                .packageName(request.getPackageName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .maxConcurrentBorrows(request.getMaxConcurrentBorrows() != null ? request.getMaxConcurrentBorrows() : 3)
                .build();
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public LibraryPackageResponse update(Long libraryPackageId, LibraryPackageRequest request) {
        LibraryPackage entity = findEntityOrThrow(libraryPackageId);
        entity.setPackageName(request.getPackageName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setDurationDays(request.getDurationDays());
        if (request.getMaxConcurrentBorrows() != null) {
            entity.setMaxConcurrentBorrows(request.getMaxConcurrentBorrows());
        }
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long libraryPackageId) {
        LibraryPackage entity = findEntityOrThrow(libraryPackageId);
        entity.setIsActive(false);
        repository.save(entity);
    }

    private LibraryPackage findEntityOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LibraryPackage", "library_package_id", id));
    }
}
