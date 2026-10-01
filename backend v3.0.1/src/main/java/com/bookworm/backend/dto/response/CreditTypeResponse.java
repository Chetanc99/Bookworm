package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreditTypeResponse {
    private Long creditTypeId;
    private String creditTypeName;
}
