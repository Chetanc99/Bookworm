package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.ProductCreditRequest;
import com.bookworm.backend.dto.response.ProductCreditResponse;
import com.bookworm.backend.entity.CreditType;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductCredit;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.ProductCreditMapper;
import com.bookworm.backend.repository.CreditTypeRepository;
import com.bookworm.backend.repository.ProductCreditRepository;
import com.bookworm.backend.repository.ProductRepository;
import com.bookworm.backend.service.ProductCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCreditServiceImpl implements ProductCreditService {

    private final ProductCreditRepository productCreditRepository;
    private final ProductRepository productRepository;
    private final CreditTypeRepository creditTypeRepository;
    private final ProductCreditMapper mapper;

    @Override
    public List<ProductCreditResponse> getByProduct(Long productId) {
        return productCreditRepository.findByProduct_ProductId(productId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ProductCreditResponse addCredit(Long productId, ProductCreditRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", productId));
        CreditType creditType = creditTypeRepository.findById(request.getCreditTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit type", "credit_type_id", request.getCreditTypeId()));

        ProductCredit credit = ProductCredit.builder()
                .product(product)
                .creditType(creditType)
                .creditValue(request.getCreditValue())
                .build();

        return mapper.toResponse(productCreditRepository.save(credit));
    }

    @Override
    @Transactional
    public ProductCreditResponse updateCredit(Long productId, Long productCreditId, ProductCreditRequest request) {
        ProductCredit credit = productCreditRepository.findById(productCreditId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCredit", "product_credit_id", productCreditId));
        if (!credit.getProduct().getProductId().equals(productId)) {
            throw new ResourceNotFoundException("ProductCredit", "product_credit_id", productCreditId);
        }
        CreditType creditType = creditTypeRepository.findById(request.getCreditTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit type", "credit_type_id", request.getCreditTypeId()));

        credit.setCreditType(creditType);
        credit.setCreditValue(request.getCreditValue());
        return mapper.toResponse(productCreditRepository.save(credit));
    }

    @Override
    @Transactional
    public void removeCredit(Long productId, Long productCreditId) {
        ProductCredit credit = productCreditRepository.findById(productCreditId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCredit", "product_credit_id", productCreditId));
        if (!credit.getProduct().getProductId().equals(productId)) {
            throw new ResourceNotFoundException("ProductCredit", "product_credit_id", productCreditId);
        }
        productCreditRepository.delete(credit);
    }
}
