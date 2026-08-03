package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.CreditTypeResponse;
import com.bookworm.backend.entity.CreditType;
import org.springframework.stereotype.Component;

@Component
public class CreditTypeMapper {
    public CreditTypeResponse toResponse(CreditType entity) {
        return CreditTypeResponse.builder()
                .creditTypeId(entity.getCreditTypeId())
                .creditTypeName(entity.getCreditTypeName())
                .build();
    }
}
