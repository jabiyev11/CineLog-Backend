package com.cinelog.dto;

import java.util.List;

public record SearchResponse(
        String message,
        List<SearchResultResponse> results
) {
}
