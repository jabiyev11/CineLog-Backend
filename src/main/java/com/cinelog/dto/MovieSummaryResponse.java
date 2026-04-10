package com.cinelog.dto;

public record MovieSummaryResponse(
        Long id,
        String title,
        int releaseYear,
        String posterImageUrl,
        Double averageRating
) {
}
