package com.bookworm.backend.repository;

import com.bookworm.backend.entity.LoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginOtpRepository extends JpaRepository<LoginOtp, Long> {
    Optional<LoginOtp> findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);
}
