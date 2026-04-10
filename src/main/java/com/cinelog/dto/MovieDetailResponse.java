package com.cinelog.dto;

import java.util.List;

public record MovieDetailResponse(
        Long id,
        String title,
        int releaseYear,
        List<String> directors,
        List<String> cast,
        List<String> genres,
        int durationMinutes,
        String country,
        String language,
        String synopsis,
        String posterImageUrl,
        Double averageRating
) {
}
