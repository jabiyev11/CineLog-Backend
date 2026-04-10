package com.cinelog.repository;

import com.cinelog.entity.WatchLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchLogRepository extends JpaRepository<WatchLog, Long> {

    List<WatchLog> findByUserIdOrderByWatchedDateDescCreatedAtDesc(Long userId);

    List<WatchLog> findByUserId(Long userId);

    long countByUserId(Long userId);

    void deleteByMovieId(Long movieId);

    void deleteByUserId(Long userId);
}
