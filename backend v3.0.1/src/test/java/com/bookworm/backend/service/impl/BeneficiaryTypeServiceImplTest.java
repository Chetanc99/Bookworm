package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.BeneficiaryTypeRequest;
import com.bookworm.backend.entity.BeneficiaryType;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.mapper.BeneficiaryTypeMapper;
import com.bookworm.backend.repository.BeneficiaryTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryTypeServiceImplTest {

    @Mock private BeneficiaryTypeRepository beneficiaryTypeRepository;
    @Mock private BeneficiaryTypeMapper mapper;

    private BeneficiaryTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BeneficiaryTypeServiceImpl(beneficiaryTypeRepository, mapper);
    }

    @Test
    void create_persistsPresetWithDefaultPercentage() {
        when(beneficiaryTypeRepository.existsByNameIgnoreCase("Author")).thenReturn(false);
        when(beneficiaryTypeRepository.save(any(BeneficiaryType.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BeneficiaryTypeRequest request = new BeneficiaryTypeRequest();
        request.setName("Author");
        request.setDefaultRoyaltyPercentage(new BigDecimal("15.00"));

        service.create(request);

        ArgumentCaptor<BeneficiaryType> captor = ArgumentCaptor.forClass(BeneficiaryType.class);
        verify(beneficiaryTypeRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Author");
        assertThat(captor.getValue().getDefaultRoyaltyPercentage()).isEqualByComparingTo("15.00");
        assertThat(captor.getValue().getIsActive()).isTrue();
    }

    @Test
    void create_duplicateNameCaseInsensitive_throws() {
        when(beneficiaryTypeRepository.existsByNameIgnoreCase("author")).thenReturn(true);

        BeneficiaryTypeRequest request = new BeneficiaryTypeRequest();
        request.setName("author");
        request.setDefaultRoyaltyPercentage(new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(DuplicateResourceException.class);
        verify(beneficiaryTypeRepository, never()).save(any());
    }
}
