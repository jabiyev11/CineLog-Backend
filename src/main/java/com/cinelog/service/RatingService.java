package com.cinelog.service;

import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.RatingRequest;
import com.cinelog.entity.Movie;
import com.cinelog.entity.Rating;
import com.cinelog.entity.User;
import com.cinelog.exception.ResourceNotFoundException;
import com.cinelog.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final CurrentUserService currentUserService;
    private final MovieService movieService;

    @Transactional
    public MessageResponse rateMovie(Long movieId, RatingRequest request) {
        validateScore(request.score());
        User user = currentUserService.getCurrentUser();
        Movie movie = movieService.getMovieEntity(movieId);

        Rating rating = ratingRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .orElseGet(Rating::new);
        rating.setUser(user);
        rating.setMovie(movie);
        rating.setScore(request.score());
        ratingRepository.save(rating);

        return new MessageResponse("Rating saved successfully.");
    }

    @Transactional
    public MessageResponse deleteRating(Long movieId) {
        User user = currentUserService.getCurrentUser();
        Rating rating = ratingRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found."));
        ratingRepository.delete(rating);
        return new MessageResponse("Rating deleted successfully.");
    }

    public void validateScore(Double score) {
        if (score == null || score < 0.5 || score > 5.0 || Math.round(score * 2) != score * 2) {
            throw new IllegalArgumentException("Rating score must be between 0.5 and 5.0 in 0.5 steps.");
        }
    }
}
