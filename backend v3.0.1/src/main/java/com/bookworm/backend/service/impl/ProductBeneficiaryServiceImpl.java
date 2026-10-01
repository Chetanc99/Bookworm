package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.ProductBeneficiaryRequest;
import com.bookworm.backend.dto.response.ProductBeneficiaryResponse;
import com.bookworm.backend.entity.Beneficiary;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductBeneficiary;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.ProductBeneficiaryMapper;
import com.bookworm.backend.repository.BeneficiaryRepository;
import com.bookworm.backend.repository.ProductBeneficiaryRepository;
import com.bookworm.backend.repository.ProductRepository;
import com.bookworm.backend.service.ProductBeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductBeneficiaryServiceImpl implements ProductBeneficiaryService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ProductBeneficiaryRepository productBeneficiaryRepository;
    private final ProductRepository productRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ProductBeneficiaryMapper mapper;

    @Override
    public List<ProductBeneficiaryResponse> getByProduct(Long productId) {
        return productBeneficiaryRepository.findByProduct_ProductId(productId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ProductBeneficiaryResponse addSplit(Long productId, ProductBeneficiaryRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", productId));
        Beneficiary beneficiary = resolveActiveBeneficiary(request.getBeneficiaryId());

        if (productBeneficiaryRepository.existsByProduct_ProductIdAndBeneficiary_BeneficiaryId(
                productId, request.getBeneficiaryId())) {
            throw new DuplicateResourceException("This beneficiary already has a royalty split on this product");
        }

        BigDecimal royaltyPercentage = resolvePercentage(beneficiary, request.getRoyaltyPercentage());

        // Business rule (not enforced by the DB schema): a product's total royalty allocation
        // must not exceed 100%, since ROYALTY_LEDGER will later pay out amount = revenue * percentage
        // per beneficiary, and an over-100% total would double-pay revenue that doesn't exist.
        BigDecimal existingTotal = productBeneficiaryRepository.sumRoyaltyPercentageByProduct(productId);
        requireWithinAllocation(existingTotal, royaltyPercentage);

        ProductBeneficiary split = ProductBeneficiary.builder()
                .product(product)
                .beneficiary(beneficiary)
                .royaltyPercentage(royaltyPercentage)
                .build();

        return mapper.toResponse(productBeneficiaryRepository.save(split));
    }

    @Override
    @Transactional
    public void removeSplit(Long productId, Long productBeneficiaryId) {
        ProductBeneficiary split = productBeneficiaryRepository.findById(productBeneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBeneficiary", "product_beneficiary_id", productBeneficiaryId));
        if (!split.getProduct().getProductId().equals(productId)) {
            throw new ResourceNotFoundException("ProductBeneficiary", "product_beneficiary_id", productBeneficiaryId);
        }
        productBeneficiaryRepository.delete(split);
    }

    @Override
    @Transactional
    public List<ProductBeneficiaryResponse> replaceAssignments(Long productId, List<ProductBeneficiaryRequest> requests) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", productId));

        if (requests.isEmpty()) {
            productBeneficiaryRepository.deleteAll(productBeneficiaryRepository.findByProduct_ProductId(productId));
            return List.of();
        }

        Set<Long> seenBeneficiaryIds = new HashSet<>();
        BigDecimal runningTotal = BigDecimal.ZERO;
        List<ProductBeneficiary> resolved = new java.util.ArrayList<>(requests.size());

        for (ProductBeneficiaryRequest request : requests) {
            if (!seenBeneficiaryIds.add(request.getBeneficiaryId())) {
                throw new DuplicateResourceException(
                        "Beneficiary " + request.getBeneficiaryId() + " is listed more than once in this request");
            }

            Beneficiary beneficiary = resolveActiveBeneficiary(request.getBeneficiaryId());
            BigDecimal royaltyPercentage = resolvePercentage(beneficiary, request.getRoyaltyPercentage());
            runningTotal = requireWithinAllocation(runningTotal, royaltyPercentage);

            resolved.add(ProductBeneficiary.builder()
                    .product(product)
                    .beneficiary(beneficiary)
                    .royaltyPercentage(royaltyPercentage)
                    .build());
        }

        // Replace wholesale rather than diff/patch existing rows - simpler and equally
        // safe, since RoyaltyLedger snapshots everything it needs (beneficiary, percentage,
        // name) at transaction time and holds no FK to ProductBeneficiary. Deleting and
        // recreating these rows cannot touch a single historical ledger entry.
        productBeneficiaryRepository.deleteAll(productBeneficiaryRepository.findByProduct_ProductId(productId));
        List<ProductBeneficiary> saved = productBeneficiaryRepository.saveAll(resolved);
        return saved.stream().map(mapper::toResponse).toList();
    }

    // New assignments only - a beneficiary that goes inactive after being assigned
    // keeps its existing ProductBeneficiary/RoyaltyLedger rows untouched (see class
    // callers); this just stops a *new* split from being created against one.
    private Beneficiary resolveActiveBeneficiary(Long beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "beneficiary_id", beneficiaryId));
        if (!Boolean.TRUE.equals(beneficiary.getIsActive())) {
            throw new IllegalArgumentException(
                    "Beneficiary '" + beneficiary.getName() + "' is inactive and cannot be assigned to a product");
        }
        return beneficiary;
    }

    // Preset is only ever a default: an explicit percentage always wins. If neither is
    // available (no percentage supplied and this beneficiary has no type), fail fast
    // rather than silently persisting a null/zero split.
    private BigDecimal resolvePercentage(Beneficiary beneficiary, BigDecimal explicitPercentage) {
        if (explicitPercentage != null) return explicitPercentage;
        if (beneficiary.getBeneficiaryType() == null) {
            throw new IllegalArgumentException(
                    "royaltyPercentage is required: beneficiary '" + beneficiary.getName()
                            + "' has no BeneficiaryType to default from");
        }
        return beneficiary.getBeneficiaryType().getDefaultRoyaltyPercentage();
    }

    private BigDecimal requireWithinAllocation(BigDecimal existingTotal, BigDecimal additionalPercentage) {
        BigDecimal newTotal = existingTotal.add(additionalPercentage);
        if (newTotal.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException(
                    "Total royalty percentage for this product would be " + newTotal
                            + "%, which exceeds 100% (currently allocated: " + existingTotal + "%)");
        }
        return newTotal;
    }
}
