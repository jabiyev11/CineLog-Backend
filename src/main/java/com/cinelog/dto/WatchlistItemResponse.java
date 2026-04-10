package com.cinelog.dto;

import java.time.LocalDateTime;

public record WatchlistItemResponse(
        Long id,
        Long movieId,
        String title,
        int releaseYear,
        String posterImageUrl,
        LocalDateTime addedAt
) {
}
