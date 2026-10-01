package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.BeneficiaryTypeRequest;
import com.bookworm.backend.dto.response.BeneficiaryTypeResponse;

import java.util.List;

public interface BeneficiaryTypeService {

    List<BeneficiaryTypeResponse> getAllActive();

    List<BeneficiaryTypeResponse> getAll();

    BeneficiaryTypeResponse getById(Long beneficiaryTypeId);

    BeneficiaryTypeResponse create(BeneficiaryTypeRequest request);

    BeneficiaryTypeResponse update(Long beneficiaryTypeId, BeneficiaryTypeRequest request);

    void delete(Long beneficiaryTypeId);
}
