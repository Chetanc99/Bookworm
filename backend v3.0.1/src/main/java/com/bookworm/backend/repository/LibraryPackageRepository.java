package com.bookworm.backend.repository;

import com.bookworm.backend.entity.LibraryPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryPackageRepository extends JpaRepository<LibraryPackage, Long> {
    List<LibraryPackage> findByIsActiveTrue();

    // Used by LibraryPackageBootstrapConfig to seed the default tiers idempotently.
    boolean existsByPackageNameIgnoreCase(String packageName);
}
