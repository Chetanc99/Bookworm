package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.ProductCreditRequest;
import com.bookworm.backend.dto.response.ProductCreditResponse;

import java.util.List;

public interface ProductCreditService {
    List<ProductCreditResponse> getByProduct(Long productId);
    ProductCreditResponse addCredit(Long productId, ProductCreditRequest request);
    void removeCredit(Long productId, Long productCreditId);
}
