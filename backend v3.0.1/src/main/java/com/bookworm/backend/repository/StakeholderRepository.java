package com.bookworm.backend.repository;

import com.bookworm.backend.entity.Stakeholder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StakeholderRepository extends JpaRepository<Stakeholder, Long> {
    List<Stakeholder> findByIsActiveTrue();
}
