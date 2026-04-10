package com.cinelog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WatchLogResponse(
        Long id,
        Long movieId,
        String title,
        int releaseYear,
        String posterImageUrl,
        LocalDate watchedDate,
        LocalDateTime createdAt
) {
}
