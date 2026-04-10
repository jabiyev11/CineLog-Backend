package com.cinelog.repository;

import com.cinelog.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    long countByReviewId(Long reviewId);

    void deleteByReviewId(Long reviewId);

    void deleteByReviewMovieId(Long movieId);

    void deleteByReviewUserId(Long userId);

    void deleteByUserId(Long userId);
}
