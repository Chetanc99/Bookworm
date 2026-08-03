package com.bookworm.backend.repository;

import com.bookworm.backend.entity.UserLibraryPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserLibraryPackageRepository extends JpaRepository<UserLibraryPackage, Long> {
    Optional<UserLibraryPackage> findByUserIdAndStatus(Long userId, UserLibraryPackage.Status status);
    Page<UserLibraryPackage> findByUserId(Long userId, Pageable pageable);

    // Used by the scheduled cleanup job that flips ACTIVE -> EXPIRED past end_date.
    List<UserLibraryPackage> findByStatusAndEndDateBefore(UserLibraryPackage.Status status, LocalDateTime now);
}
