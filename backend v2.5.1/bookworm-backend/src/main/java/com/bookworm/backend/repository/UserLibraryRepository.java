package com.bookworm.backend.repository;

import com.bookworm.backend.entity.UserLibrary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserLibraryRepository extends JpaRepository<UserLibrary, Long> {
    List<UserLibrary> findByUserLibraryPackage_UserLibraryPackageIdAndStatus(
            Long userLibraryPackageId, UserLibrary.Status status);

    Page<UserLibrary> findByUserLibraryPackage_UserId(Long userId, Pageable pageable);

    long countByUserLibraryPackage_UserLibraryPackageIdAndStatus(
            Long userLibraryPackageId, UserLibrary.Status status);

    // Used by the scheduled cleanup job that flips BORROWED -> EXPIRED past due_date.
    List<UserLibrary> findByStatusAndDueDateBefore(UserLibrary.Status status, LocalDateTime now);
}
