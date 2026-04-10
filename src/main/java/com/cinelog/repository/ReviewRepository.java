package com.cinelog.repository;

import com.cinelog.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    Optional<Review> findByIdAndMovieId(Long reviewId, Long movieId);

    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);

    void deleteByMovieId(Long movieId);

    void deleteByUserId(Long userId);
}
