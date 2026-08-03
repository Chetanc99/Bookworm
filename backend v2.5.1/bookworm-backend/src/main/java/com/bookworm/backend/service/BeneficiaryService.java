package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.BeneficiaryRequest;
import com.bookworm.backend.dto.response.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {
    List<BeneficiaryResponse> getAllActive();
    List<BeneficiaryResponse> getAll();
    BeneficiaryResponse getById(Long beneficiaryId);
    BeneficiaryResponse create(BeneficiaryRequest request);
    BeneficiaryResponse update(Long beneficiaryId, BeneficiaryRequest request);
    void delete(Long beneficiaryId);
}
