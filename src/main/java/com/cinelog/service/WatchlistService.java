package com.cinelog.service;

import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.WatchlistItemResponse;
import com.cinelog.entity.Movie;
import com.cinelog.entity.User;
import com.cinelog.entity.Watchlist;
import com.cinelog.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final CurrentUserService currentUserService;
    private final MovieService movieService;

    @Transactional
    public MessageResponse addToWatchlist(Long movieId) {
        User user = currentUserService.getCurrentUser();
        Movie movie = movieService.getMovieEntity(movieId);
        if (watchlistRepository.findByUserIdAndMovieId(user.getId(), movieId).isEmpty()) {
            Watchlist watchlist = new Watchlist();
            watchlist.setUser(user);
            watchlist.setMovie(movie);
            watchlistRepository.save(watchlist);
        }
        return new MessageResponse("Movie added to watchlist.");
    }

    @Transactional
    public MessageResponse removeFromWatchlist(Long movieId) {
        User user = currentUserService.getCurrentUser();
        watchlistRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .ifPresent(watchlistRepository::delete);
        return new MessageResponse("Movie removed from watchlist.");
    }

    public List<WatchlistItemResponse> getWatchlist() {
        User user = currentUserService.getCurrentUser();
        return watchlistRepository.findByUserIdOrderByAddedAtDesc(user.getId())
                .stream()
                .map(item -> new WatchlistItemResponse(
                        item.getId(),
                        item.getMovie().getId(),
                        item.getMovie().getTitle(),
                        item.getMovie().getReleaseYear(),
                        item.getMovie().getPosterImageUrl(),
                        item.getAddedAt()))
                .toList();
    }
}
