package com.cinelog.repository;

import com.cinelog.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    Optional<Watchlist> findByUserIdAndMovieId(Long userId, Long movieId);

    List<Watchlist> findByUserIdOrderByAddedAtDesc(Long userId);

    void deleteByMovieId(Long movieId);

    void deleteByUserId(Long userId);
}
