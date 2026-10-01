package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.ProductBeneficiaryRequest;
import com.bookworm.backend.dto.response.ProductBeneficiaryResponse;

import java.util.List;

public interface ProductBeneficiaryService {
    List<ProductBeneficiaryResponse> getByProduct(Long productId);
    ProductBeneficiaryResponse addSplit(Long productId, ProductBeneficiaryRequest request);
    void removeSplit(Long productId, Long productBeneficiaryId);

    /**
     * Replaces every royalty split on a product with exactly the given list in
     * one transaction - used by product create/update when the caller supplies
     * a full beneficiaries list, and by the standalone bulk-replace endpoint.
     * Never touches RoyaltyLedger: past rows aren't linked to ProductBeneficiary
     * rows at all (only to Beneficiary + a frozen percentage/name snapshot), so
     * deleting and recreating splits here cannot alter historical accruals.
     */
    List<ProductBeneficiaryResponse> replaceAssignments(Long productId, List<ProductBeneficiaryRequest> requests);
}
