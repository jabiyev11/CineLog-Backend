package com.cinelog.dto;

public record UserProfileResponse(
        String username,
        String profilePictureUrl,
        String bio,
        long totalMoviesWatched
) {
}
