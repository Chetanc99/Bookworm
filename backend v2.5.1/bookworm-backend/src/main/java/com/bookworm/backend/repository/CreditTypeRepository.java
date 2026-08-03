package com.bookworm.backend.repository;

import com.bookworm.backend.entity.CreditType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTypeRepository extends JpaRepository<CreditType, Long> {
    boolean existsByCreditTypeNameIgnoreCase(String creditTypeName);
}
