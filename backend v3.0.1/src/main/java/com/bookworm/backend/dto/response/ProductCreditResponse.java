package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductCreditResponse {
    private Long productCreditId;
    private Long creditTypeId;
    private String creditTypeName;
    private String creditValue;
}
