package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.ProductStakeholderRequest;
import com.bookworm.backend.dto.response.ProductStakeholderResponse;

import java.util.List;

public interface ProductStakeholderService {
    List<ProductStakeholderResponse> getByProduct(Long productId);
    ProductStakeholderResponse addCredit(Long productId, ProductStakeholderRequest request);
    void removeCredit(Long productId, Long productStakeholderId);
}
