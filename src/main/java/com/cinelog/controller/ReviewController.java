package com.cinelog.controller;

import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.ReviewCreateRequest;
import com.cinelog.dto.ReviewResponse;
import com.cinelog.dto.ReviewUpdateRequest;
import com.cinelog.dto.ToggleLikeResponse;
import com.cinelog.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/movies/{id}/reviews")
    public List<ReviewResponse> getMovieReviews(@PathVariable Long id) {
        return reviewService.getMovieReviews(id);
    }

    @PostMapping("/movies/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@PathVariable Long id, @Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.createReview(id, request);
    }

    @PutMapping("/movies/{id}/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable Long id,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return reviewService.updateReview(id, reviewId, request);
    }

    @DeleteMapping("/movies/{id}/reviews/{reviewId}")
    public MessageResponse deleteReview(@PathVariable Long id, @PathVariable Long reviewId) {
        return reviewService.deleteReview(id, reviewId);
    }

    @PostMapping("/reviews/{reviewId}/like")
    public ToggleLikeResponse toggleLike(@PathVariable Long reviewId) {
        return reviewService.toggleLike(reviewId);
    }
}
