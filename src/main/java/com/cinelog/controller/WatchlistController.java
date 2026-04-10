package com.cinelog.controller;

import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.WatchlistItemResponse;
import com.cinelog.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping("/watchlist/{movieId}")
    public MessageResponse addToWatchlist(@PathVariable Long movieId) {
        return watchlistService.addToWatchlist(movieId);
    }

    @DeleteMapping("/watchlist/{movieId}")
    public MessageResponse removeFromWatchlist(@PathVariable Long movieId) {
        return watchlistService.removeFromWatchlist(movieId);
    }

    @GetMapping("/watchlist")
    public List<WatchlistItemResponse> getWatchlist() {
        return watchlistService.getWatchlist();
    }
}
