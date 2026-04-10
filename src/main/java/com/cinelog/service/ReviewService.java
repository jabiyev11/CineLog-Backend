package com.cinelog.service;

import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.ReviewCreateRequest;
import com.cinelog.dto.ReviewResponse;
import com.cinelog.dto.ReviewUpdateRequest;
import com.cinelog.dto.ToggleLikeResponse;
import com.cinelog.entity.Movie;
import com.cinelog.entity.Rating;
import com.cinelog.entity.Review;
import com.cinelog.entity.ReviewLike;
import com.cinelog.entity.User;
import com.cinelog.exception.DuplicateResourceException;
import com.cinelog.exception.ForbiddenException;
import com.cinelog.exception.ResourceNotFoundException;
import com.cinelog.repository.RatingRepository;
import com.cinelog.repository.ReviewLikeRepository;
import com.cinelog.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final RatingRepository ratingRepository;
    private final CurrentUserService currentUserService;
    private final MovieService movieService;
    private final RatingService ratingService;

    public List<ReviewResponse> getMovieReviews(Long movieId) {
        movieService.getMovieEntity(movieId);
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse createReview(Long movieId, ReviewCreateRequest request) {
        User user = currentUserService.getCurrentUser();
        Movie movie = movieService.getMovieEntity(movieId);

        if (reviewRepository.findByUserIdAndMovieId(user.getId(), movieId).isPresent()) {
            throw new DuplicateResourceException("You have already reviewed this movie.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setText(request.text().trim());
        Review saved = reviewRepository.save(review);

        if (request.score() != null) {
            ratingService.validateScore(request.score());
            Rating rating = ratingRepository.findByUserIdAndMovieId(user.getId(), movieId).orElseGet(Rating::new);
            rating.setUser(user);
            rating.setMovie(movie);
            rating.setScore(request.score());
            ratingRepository.save(rating);
        }

        return toResponse(saved);
    }

    @Transactional
    public ReviewResponse updateReview(Long movieId, Long reviewId, ReviewUpdateRequest request) {
        User user = currentUserService.getCurrentUser();
        Review review = reviewRepository.findByIdAndMovieId(reviewId, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only edit your own review.");
        }
        review.setText(request.text().trim());
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public MessageResponse deleteReview(Long movieId, Long reviewId) {
        User user = currentUserService.getCurrentUser();
        Review review = reviewRepository.findByIdAndMovieId(reviewId, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only delete your own review.");
        }
        reviewLikeRepository.deleteByReviewId(reviewId);
        reviewRepository.delete(review);
        return new MessageResponse("Review deleted successfully.");
    }

    @Transactional
    public ToggleLikeResponse toggleLike(Long reviewId) {
        User user = currentUserService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));

        boolean liked;
        ReviewLike existing = reviewLikeRepository.findByUserIdAndReviewId(user.getId(), reviewId).orElse(null);
        if (existing != null) {
            reviewLikeRepository.delete(existing);
            liked = false;
        } else {
            ReviewLike reviewLike = new ReviewLike();
            reviewLike.setUser(user);
            reviewLike.setReview(review);
            reviewLikeRepository.save(reviewLike);
            liked = true;
        }
        return new ToggleLikeResponse(liked, reviewLikeRepository.countByReviewId(reviewId));
    }

    private ReviewResponse toResponse(Review review) {
        Double rating = ratingRepository.findByUserIdAndMovieId(review.getUser().getId(), review.getMovie().getId())
                .map(Rating::getScore)
                .orElse(null);
        long likeCount = reviewLikeRepository.countByReviewId(review.getId());
        return new ReviewResponse(
                review.getId(),
                review.getUser().getUsername(),
                rating,
                review.getText(),
                review.getUpdatedAt(),
                likeCount
        );
    }
}
