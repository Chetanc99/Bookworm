package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.AdminTransactionResponse;
import com.bookworm.backend.dto.response.RevenueSummaryResponse;
import com.bookworm.backend.repository.AdminTransactionRepository;
import com.bookworm.backend.service.AdminTransactionService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTransactionServiceImpl implements AdminTransactionService {

    private final AdminTransactionRepository adminTransactionRepository;

    @Override
    public Page<AdminTransactionResponse> getAll(Pageable pageable) {
        List<Tuple> rows = adminTransactionRepository.findPage(pageable.getPageSize(), (int) pageable.getOffset());
        long total = adminTransactionRepository.countAll();

        List<AdminTransactionResponse> content = rows.stream().map(this::toResponse).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public RevenueSummaryResponse getRevenueSummary() {
        Tuple row = adminTransactionRepository.getRevenueSummary();
        return RevenueSummaryResponse.builder()
                .totalRevenue((BigDecimal) row.get("total_revenue"))
                .purchaseCount(((Number) row.get("purchase_count")).longValue())
                .rentCount(((Number) row.get("rent_count")).longValue())
                .build();
    }

    private AdminTransactionResponse toResponse(Tuple row) {
        Object createdAtRaw = row.get("created_at");
        LocalDateTime createdAt = createdAtRaw instanceof Timestamp timestamp
                ? timestamp.toLocalDateTime()
                : (LocalDateTime) createdAtRaw;

        return AdminTransactionResponse.builder()
                .transactionType((String) row.get("transaction_type"))
                .transactionId(((Number) row.get("id")).longValue())
                .userId(((Number) row.get("user_id")).longValue())
                .userEmail((String) row.get("user_email"))
                .userFullName((String) row.get("user_full_name"))
                .totalAmount((BigDecimal) row.get("total_amount"))
                .status((String) row.get("status"))
                .createdAt(createdAt)
                .build();
    }
}
