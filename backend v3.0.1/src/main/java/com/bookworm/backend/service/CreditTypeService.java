package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.CreditTypeRequest;
import com.bookworm.backend.dto.response.CreditTypeResponse;

import java.util.List;

public interface CreditTypeService {
    List<CreditTypeResponse> getAll();
    CreditTypeResponse getById(Long creditTypeId);
    CreditTypeResponse create(CreditTypeRequest request);
    CreditTypeResponse update(Long creditTypeId, CreditTypeRequest request);
    void delete(Long creditTypeId);
}
