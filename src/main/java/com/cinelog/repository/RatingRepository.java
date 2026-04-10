package com.cinelog.repository;

import com.cinelog.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndMovieId(Long userId, Long movieId);

    void deleteByUserIdAndMovieId(Long userId, Long movieId);

    void deleteByMovieId(Long movieId);

    void deleteByUserId(Long userId);

    @Query("select avg(r.score) from Rating r where r.movie.id = :movieId")
    Double findAverageScoreByMovieId(Long movieId);
}
