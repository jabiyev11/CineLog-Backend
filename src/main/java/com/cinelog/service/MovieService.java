package com.cinelog.service;

import com.cinelog.dto.MovieDetailResponse;
import com.cinelog.dto.MovieRequest;
import com.cinelog.dto.MovieSummaryResponse;
import com.cinelog.dto.PageResponse;
import com.cinelog.dto.SearchResponse;
import com.cinelog.dto.SearchResultResponse;
import com.cinelog.entity.Movie;
import com.cinelog.exception.BadRequestException;
import com.cinelog.exception.ResourceNotFoundException;
import com.cinelog.repository.MovieRepository;
import com.cinelog.repository.RatingRepository;
import com.cinelog.repository.ReviewLikeRepository;
import com.cinelog.repository.ReviewRepository;
import com.cinelog.repository.WatchLogRepository;
import com.cinelog.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final WatchlistRepository watchlistRepository;
    private final WatchLogRepository watchLogRepository;

    public PageResponse<MovieSummaryResponse> getMovies(Pageable pageable) {
        Page<MovieSummaryResponse> page = movieRepository.findAll(pageable)
                .map(this::toMovieSummaryResponse);
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public MovieDetailResponse getMovie(Long movieId) {
        Movie movie = getMovieEntity(movieId);
        return toMovieDetailResponse(movie);
    }

    public SearchResponse searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Search query must not be blank.");
        }
        List<SearchResultResponse> results = movieRepository.search(query.trim()).stream()
                .map(movie -> new SearchResultResponse(movie.getId(), movie.getTitle(), movie.getReleaseYear(), movie.getPosterImageUrl()))
                .toList();
        String message = results.isEmpty() ? "No movies found matching your query." : null;
        return new SearchResponse(message, results);
    }

    @Transactional
    public MovieDetailResponse createMovie(MovieRequest request) {
        Movie movie = new Movie();
        applyMovieRequest(movie, request);
        return toMovieDetailResponse(movieRepository.save(movie));
    }

    @Transactional
    public MovieDetailResponse updateMovie(Long movieId, MovieRequest request) {
        Movie movie = getMovieEntity(movieId);
        applyMovieRequest(movie, request);
        return toMovieDetailResponse(movieRepository.save(movie));
    }

    @Transactional
    public void deleteMovie(Long movieId) {
        Movie movie = getMovieEntity(movieId);
        reviewLikeRepository.deleteByReviewMovieId(movieId);
        ratingRepository.deleteByMovieId(movieId);
        reviewRepository.deleteByMovieId(movieId);
        watchlistRepository.deleteByMovieId(movieId);
        watchLogRepository.deleteByMovieId(movieId);
        movieRepository.delete(movie);
    }

    public Movie getMovieEntity(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found."));
    }

    public Double getRoundedAverageRating(Long movieId) {
        Double avg = ratingRepository.findAverageScoreByMovieId(movieId);
        if (avg == null) {
            return null;
        }
        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private void applyMovieRequest(Movie movie, MovieRequest request) {
        movie.setTitle(request.title().trim());
        movie.setReleaseYear(request.releaseYear());
        movie.setDirectors(request.directors().stream().map(String::trim).toList());
        movie.setCastMembers(request.cast().stream().map(String::trim).toList());
        movie.setGenres(request.genres().stream().map(String::trim).toList());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setCountry(request.country().trim());
        movie.setLanguage(request.language().trim());
        movie.setSynopsis(request.synopsis().trim());
        movie.setPosterImageUrl(request.posterImageUrl().trim());
        movie.setBackdropImageUrl(request.backdropImageUrl() == null || request.backdropImageUrl().isBlank()
                ? null
                : request.backdropImageUrl().trim());
        movie.setImageUrls(request.imageUrls() == null
                ? new ArrayList<>()
                : request.imageUrls().stream().map(String::trim).toList());
    }

    private MovieSummaryResponse toMovieSummaryResponse(Movie movie) {
        return new MovieSummaryResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getReleaseYear(),
                movie.getPosterImageUrl(),
                getRoundedAverageRating(movie.getId())
        );
    }

    private MovieDetailResponse toMovieDetailResponse(Movie movie) {
        return new MovieDetailResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getReleaseYear(),
                movie.getDirectors(),
                movie.getCastMembers(),
                movie.getGenres(),
                movie.getDurationMinutes(),
                movie.getCountry(),
                movie.getLanguage(),
                movie.getSynopsis(),
                movie.getPosterImageUrl(),
                movie.getBackdropImageUrl(),
                movie.getImageUrls(),
                getRoundedAverageRating(movie.getId())
        );
    }
}
