package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.LoyaltyBalanceResponse;
import com.bookworm.backend.dto.response.LoyaltyLedgerResponse;
import com.bookworm.backend.entity.LoyaltyLedger;
import com.bookworm.backend.mapper.LoyaltyMapper;
import com.bookworm.backend.repository.LoyaltyLedgerRepository;
import com.bookworm.backend.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoyaltyServiceImpl implements LoyaltyService {

    private final LoyaltyLedgerRepository loyaltyLedgerRepository;
    private final LoyaltyMapper mapper;

    @Override
    public Page<LoyaltyLedgerResponse> getHistory(Long userId, Pageable pageable) {
        return loyaltyLedgerRepository.findByUserId(userId, pageable).map(mapper::toResponse);
    }

    @Override
    public LoyaltyBalanceResponse getBalance(Long userId) {
        int balance = loyaltyLedgerRepository.findByUserId(userId).stream()
                .mapToInt(entry -> entry.getEntryType() == LoyaltyLedger.EntryType.REDEEM
                        ? -entry.getPoints()
                        : entry.getPoints())
                .sum();

        return LoyaltyBalanceResponse.builder()
                .userId(userId)
                .balance(balance)
                .build();
    }
}
