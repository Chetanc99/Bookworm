package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.BeneficiaryTypeRequest;
import com.bookworm.backend.dto.response.BeneficiaryTypeResponse;
import com.bookworm.backend.entity.BeneficiaryType;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.BeneficiaryTypeMapper;
import com.bookworm.backend.repository.BeneficiaryTypeRepository;
import com.bookworm.backend.service.BeneficiaryTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for royalty presets (Author/Publisher/Narrator/etc). Mirrors
 * BeneficiaryServiceImpl's shape exactly - same soft-delete-via-isActive
 * reasoning, since a type may already be referenced by existing
 * Beneficiary rows and a hard delete would break that FK.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BeneficiaryTypeServiceImpl implements BeneficiaryTypeService {

    private final BeneficiaryTypeRepository beneficiaryTypeRepository;
    private final BeneficiaryTypeMapper mapper;

    @Override
    public List<BeneficiaryTypeResponse> getAllActive() {
        return beneficiaryTypeRepository.findByIsActiveTrue().stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<BeneficiaryTypeResponse> getAll() {
        return beneficiaryTypeRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public BeneficiaryTypeResponse getById(Long beneficiaryTypeId) {
        return mapper.toResponse(findEntityOrThrow(beneficiaryTypeId));
    }

    @Override
    @Transactional
    public BeneficiaryTypeResponse create(BeneficiaryTypeRequest request) {
        if (beneficiaryTypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A beneficiary type named '" + request.getName() + "' already exists");
        }
        BeneficiaryType type = BeneficiaryType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .defaultRoyaltyPercentage(request.getDefaultRoyaltyPercentage())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return mapper.toResponse(beneficiaryTypeRepository.save(type));
    }

    @Override
    @Transactional
    public BeneficiaryTypeResponse update(Long beneficiaryTypeId, BeneficiaryTypeRequest request) {
        BeneficiaryType type = findEntityOrThrow(beneficiaryTypeId);
        if (!type.getName().equalsIgnoreCase(request.getName())
                && beneficiaryTypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A beneficiary type named '" + request.getName() + "' already exists");
        }
        type.setName(request.getName());
        type.setDescription(request.getDescription());
        // Changing the preset here never touches percentages already snapshotted
        // onto existing ProductBeneficiary rows - only future addSplit() calls
        // that omit an explicit percentage will pick up the new default.
        type.setDefaultRoyaltyPercentage(request.getDefaultRoyaltyPercentage());
        if (request.getIsActive() != null) type.setIsActive(request.getIsActive());
        return mapper.toResponse(beneficiaryTypeRepository.save(type));
    }

    @Override
    @Transactional
    public void delete(Long beneficiaryTypeId) {
        BeneficiaryType type = findEntityOrThrow(beneficiaryTypeId);
        type.setIsActive(false);
        beneficiaryTypeRepository.save(type);
    }

    private BeneficiaryType findEntityOrThrow(Long beneficiaryTypeId) {
        return beneficiaryTypeRepository.findById(beneficiaryTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("BeneficiaryType", "beneficiary_type_id", beneficiaryTypeId));
    }
}
