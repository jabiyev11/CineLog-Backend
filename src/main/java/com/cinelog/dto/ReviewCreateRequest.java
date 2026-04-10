package com.cinelog.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotBlank @Size(min = 1, max = 500) String text,
        @DecimalMin(value = "0.5")
        @DecimalMax(value = "5.0")
        Double score
) {
}
