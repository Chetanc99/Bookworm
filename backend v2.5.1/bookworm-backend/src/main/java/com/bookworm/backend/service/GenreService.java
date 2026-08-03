package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.GenreRequest;
import com.bookworm.backend.dto.response.GenreResponse;

import java.util.List;

public interface GenreService {
    List<GenreResponse> getAll();
    List<GenreResponse> getBySubcategory(Long subcategoryId);
    GenreResponse getById(Long genreId);
    GenreResponse create(GenreRequest request);
    GenreResponse update(Long genreId, GenreRequest request);
    void delete(Long genreId);
}
