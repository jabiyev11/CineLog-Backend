package com.cinelog.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RatingRequest(
        @NotNull
        @DecimalMin(value = "0.5")
        @DecimalMax(value = "5.0")
        Double score
) {
}
