package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.ProductStakeholderRequest;
import com.bookworm.backend.dto.response.ProductStakeholderResponse;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductStakeholder;
import com.bookworm.backend.entity.Stakeholder;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.ProductStakeholderMapper;
import com.bookworm.backend.repository.ProductRepository;
import com.bookworm.backend.repository.ProductStakeholderRepository;
import com.bookworm.backend.repository.StakeholderRepository;
import com.bookworm.backend.service.ProductStakeholderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductStakeholderServiceImpl implements ProductStakeholderService {

    private final ProductStakeholderRepository productStakeholderRepository;
    private final ProductRepository productRepository;
    private final StakeholderRepository stakeholderRepository;
    private final ProductStakeholderMapper mapper;

    @Override
    public List<ProductStakeholderResponse> getByProduct(Long productId) {
        return productStakeholderRepository.findByProduct_ProductId(productId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ProductStakeholderResponse addCredit(Long productId, ProductStakeholderRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", productId));
        Stakeholder stakeholder = stakeholderRepository.findById(request.getStakeholderId())
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "stakeholder_id", request.getStakeholderId()));

        if (productStakeholderRepository.existsByProduct_ProductIdAndStakeholder_StakeholderIdAndRole(
                productId, request.getStakeholderId(), request.getRole())) {
            throw new DuplicateResourceException(
                    "This stakeholder is already credited with this role on this product");
        }

        ProductStakeholder credit = ProductStakeholder.builder()
                .product(product)
                .stakeholder(stakeholder)
                .role(request.getRole())
                .build();

        return mapper.toResponse(productStakeholderRepository.save(credit));
    }

    @Override
    @Transactional
    public void removeCredit(Long productId, Long productStakeholderId) {
        ProductStakeholder credit = productStakeholderRepository.findById(productStakeholderId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductStakeholder", "product_stakeholder_id", productStakeholderId));
        if (!credit.getProduct().getProductId().equals(productId)) {
            throw new ResourceNotFoundException(
                    "ProductStakeholder", "product_stakeholder_id", productStakeholderId);
        }
        // Hard delete is correct here - this is a pure attribution join row, not a business
        // record with downstream references (unlike Products/Categories which other tables point to).
        productStakeholderRepository.delete(credit);
    }
}
