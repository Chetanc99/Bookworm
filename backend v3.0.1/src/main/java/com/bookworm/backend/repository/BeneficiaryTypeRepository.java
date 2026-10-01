package com.bookworm.backend.repository;

import com.bookworm.backend.entity.BeneficiaryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryTypeRepository extends JpaRepository<BeneficiaryType, Long> {

    List<BeneficiaryType> findByIsActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}
