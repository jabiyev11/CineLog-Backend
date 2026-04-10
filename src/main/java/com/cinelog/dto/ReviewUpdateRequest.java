package com.cinelog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequest(
        @NotBlank @Size(min = 1, max = 500) String text
) {
}
