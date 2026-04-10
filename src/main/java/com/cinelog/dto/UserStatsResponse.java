package com.cinelog.dto;

import java.util.Map;

public record UserStatsResponse(
        String mostWatchedGenre,
        String mostWatchedDirector,
        Map<Integer, Long> moviesWatchedPerYear
) {
}
