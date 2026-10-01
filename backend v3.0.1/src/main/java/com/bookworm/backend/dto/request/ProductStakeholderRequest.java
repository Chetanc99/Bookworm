package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductStakeholderRequest {

    @NotNull(message = "stakeholderId is required")
    private Long stakeholderId;

    @Size(max = 50)
    private String role;
}
