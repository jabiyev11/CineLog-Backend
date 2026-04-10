package com.cinelog.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String username,
        Double rating,
        String text,
        LocalDateTime date,
        long likeCount
) {
}
