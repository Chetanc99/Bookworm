package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.CreditTypeRequest;
import com.bookworm.backend.dto.response.CreditTypeResponse;
import com.bookworm.backend.entity.CreditType;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.CreditTypeMapper;
import com.bookworm.backend.repository.CreditTypeRepository;
import com.bookworm.backend.service.CreditTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditTypeServiceImpl implements CreditTypeService {

    private final CreditTypeRepository creditTypeRepository;
    private final CreditTypeMapper creditTypeMapper;

    @Override
    public List<CreditTypeResponse> getAll() {
        return creditTypeRepository.findAll().stream().map(creditTypeMapper::toResponse).toList();
    }

    @Override
    public CreditTypeResponse getById(Long creditTypeId) {
        return creditTypeMapper.toResponse(findEntityOrThrow(creditTypeId));
    }

    @Override
    @Transactional
    public CreditTypeResponse create(CreditTypeRequest request) {
        if (creditTypeRepository.existsByCreditTypeNameIgnoreCase(request.getCreditTypeName())) {
            throw new DuplicateResourceException("Credit type '" + request.getCreditTypeName() + "' already exists");
        }
        CreditType creditType = CreditType.builder().creditTypeName(request.getCreditTypeName()).build();
        return creditTypeMapper.toResponse(creditTypeRepository.save(creditType));
    }

    @Override
    @Transactional
    public CreditTypeResponse update(Long creditTypeId, CreditTypeRequest request) {
        CreditType creditType = findEntityOrThrow(creditTypeId);
        if (!creditType.getCreditTypeName().equalsIgnoreCase(request.getCreditTypeName())
                && creditTypeRepository.existsByCreditTypeNameIgnoreCase(request.getCreditTypeName())) {
            throw new DuplicateResourceException("Credit type '" + request.getCreditTypeName() + "' already exists");
        }
        creditType.setCreditTypeName(request.getCreditTypeName());
        return creditTypeMapper.toResponse(creditTypeRepository.save(creditType));
    }

    @Override
    @Transactional
    public void delete(Long creditTypeId) {
        CreditType creditType = findEntityOrThrow(creditTypeId);
        try {
            creditTypeRepository.delete(creditType);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException(
                    "Cannot delete credit type '" + creditType.getCreditTypeName()
                            + "' - it is still in use by one or more products");
        }
    }

    private CreditType findEntityOrThrow(Long creditTypeId) {
        return creditTypeRepository.findById(creditTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit type", "credit_type_id", creditTypeId));
    }
}
