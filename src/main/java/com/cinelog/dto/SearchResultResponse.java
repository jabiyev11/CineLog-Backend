package com.cinelog.dto;

public record SearchResultResponse(
        Long id,
        String title,
        int releaseYear,
        String posterImageUrl
) {
}
