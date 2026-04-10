package com.cinelog.dto;

public record ToggleLikeResponse(
        boolean liked,
        long likeCount
) {
}
