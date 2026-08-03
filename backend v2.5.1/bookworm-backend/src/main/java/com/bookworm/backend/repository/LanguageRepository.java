package com.bookworm.backend.repository;

import com.bookworm.backend.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    List<Language> findByIsActiveTrue();

    boolean existsByLanguageNameIgnoreCase(String languageName);

    Optional<Language> findByLanguageNameIgnoreCase(String languageName);
}
