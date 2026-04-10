package com.cinelog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MovieRequest(
        @NotBlank String title,
        @NotNull @Min(1888) Integer releaseYear,
        @NotEmpty List<@NotBlank String> directors,
        @NotEmpty List<@NotBlank String> cast,
        @NotEmpty List<@NotBlank String> genres,
        @NotNull @Min(1) Integer durationMinutes,
        @NotBlank String country,
        @NotBlank String language,
        @NotBlank @Size(max = 4000) String synopsis,
        @NotBlank String posterImageUrl,
        String backdropImageUrl,
        List<@NotBlank String> imageUrls
) {
}
