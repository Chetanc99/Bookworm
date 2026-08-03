package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.LibraryPackageRequest;
import com.bookworm.backend.dto.response.LibraryPackageResponse;

import java.util.List;

public interface LibraryPackageService {
    List<LibraryPackageResponse> getAllActive();
    LibraryPackageResponse getById(Long libraryPackageId);
    LibraryPackageResponse create(LibraryPackageRequest request);
    LibraryPackageResponse update(Long libraryPackageId, LibraryPackageRequest request);
    void delete(Long libraryPackageId);
}
