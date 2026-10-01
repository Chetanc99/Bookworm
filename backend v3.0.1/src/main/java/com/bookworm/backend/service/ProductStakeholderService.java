package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.ProductStakeholderRequest;
import com.bookworm.backend.dto.response.ProductStakeholderResponse;

import java.util.List;

public interface ProductStakeholderService {
    List<ProductStakeholderResponse> getByProduct(Long productId);
    ProductStakeholderResponse addCredit(Long productId, ProductStakeholderRequest request);
    /** Edit an existing credit's stakeholder/role in place (same row id) - distinct from remove+add, which would change productStakeholderId. */
    ProductStakeholderResponse updateCredit(Long productId, Long productStakeholderId, ProductStakeholderRequest request);
    void removeCredit(Long productId, Long productStakeholderId);
}
