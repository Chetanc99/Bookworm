package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.GenreRequest;
import com.bookworm.backend.dto.response.GenreResponse;
import com.bookworm.backend.entity.Genre;
import com.bookworm.backend.entity.ProductSubcategory;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.GenreMapper;
import com.bookworm.backend.repository.GenreRepository;
import com.bookworm.backend.repository.ProductSubcategoryRepository;
import com.bookworm.backend.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final ProductSubcategoryRepository subcategoryRepository;
    private final GenreMapper genreMapper;

    @Override
    public List<GenreResponse> getAll() {
        return genreRepository.findByIsActiveTrue().stream().map(genreMapper::toResponse).toList();
    }

    @Override
    public List<GenreResponse> getBySubcategory(Long subcategoryId) {
        return genreRepository.findBySubcategory_SubcategoryIdAndIsActiveTrue(subcategoryId)
                .stream().map(genreMapper::toResponse).toList();
    }

    @Override
    public GenreResponse getById(Long genreId) {
        return genreMapper.toResponse(findEntityOrThrow(genreId));
    }

    @Override
    @Transactional
    public GenreResponse create(GenreRequest request) {
        ProductSubcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategory_id", request.getSubcategoryId()));

        if (genreRepository.existsByGenreNameIgnoreCaseAndSubcategory_SubcategoryId(
                request.getGenreName(), request.getSubcategoryId())) {
            throw new DuplicateResourceException(
                    "Genre '" + request.getGenreName() + "' already exists under this subcategory");
        }

        Genre genre = Genre.builder()
                .subcategory(subcategory)
                .genreName(request.getGenreName())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return genreMapper.toResponse(genreRepository.save(genre));
    }

    @Override
    @Transactional
    public GenreResponse update(Long genreId, GenreRequest request) {
        Genre genre = findEntityOrThrow(genreId);

        if (!genre.getSubcategory().getSubcategoryId().equals(request.getSubcategoryId())) {
            ProductSubcategory newSubcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategory_id", request.getSubcategoryId()));
            genre.setSubcategory(newSubcategory);
        }

        genre.setGenreName(request.getGenreName());
        if (request.getIsActive() != null) genre.setIsActive(request.getIsActive());

        return genreMapper.toResponse(genreRepository.save(genre));
    }

    @Override
    @Transactional
    public void delete(Long genreId) {
        Genre genre = findEntityOrThrow(genreId);
        // Soft delete - PRODUCTS.genre_id references this table (nullable FK, but still ON DELETE RESTRICT
        // per the schema, so a hard delete would fail once any product uses this genre).
        genre.setIsActive(false);
        genreRepository.save(genre);
    }

    private Genre findEntityOrThrow(Long genreId) {
        return genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "genre_id", genreId));
    }
}
