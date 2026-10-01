package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.ProductCreditResponse;
import com.bookworm.backend.entity.ProductCredit;
import org.springframework.stereotype.Component;

@Component
public class ProductCreditMapper {
    public ProductCreditResponse toResponse(ProductCredit entity) {
        return ProductCreditResponse.builder()
                .productCreditId(entity.getProductCreditId())
                .creditTypeId(entity.getCreditType().getCreditTypeId())
                .creditTypeName(entity.getCreditType().getCreditTypeName())
                .creditValue(entity.getCreditValue())
                .build();
    }
}
