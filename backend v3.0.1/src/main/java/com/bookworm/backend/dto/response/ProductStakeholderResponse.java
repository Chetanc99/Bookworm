package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductStakeholderResponse {
    private Long productStakeholderId;
    private Long stakeholderId;
    private String stakeholderName;
    private String stakeholderType;
    private String role;
}
