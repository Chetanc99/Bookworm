package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditTypeRequest {

    @NotBlank(message = "Credit type name is required")
    @Size(max = 50)
    private String creditTypeName;
}
