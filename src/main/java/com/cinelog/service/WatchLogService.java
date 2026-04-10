package com.cinelog.service;

import com.cinelog.dto.WatchLogRequest;
import com.cinelog.dto.WatchLogResponse;
import com.cinelog.entity.Movie;
import com.cinelog.entity.User;
import com.cinelog.entity.WatchLog;
import com.cinelog.repository.WatchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchLogService {

    private final WatchLogRepository watchLogRepository;
    private final CurrentUserService currentUserService;
    private final MovieService movieService;

    @Transactional
    public WatchLogResponse logWatch(Long movieId, WatchLogRequest request) {
        User user = currentUserService.getCurrentUser();
        Movie movie = movieService.getMovieEntity(movieId);

        WatchLog watchLog = new WatchLog();
        watchLog.setUser(user);
        watchLog.setMovie(movie);
        watchLog.setWatchedDate(request.watchedDate());
        return toResponse(watchLogRepository.save(watchLog));
    }

    public List<WatchLogResponse> getWatchHistory() {
        User user = currentUserService.getCurrentUser();
        return watchLogRepository.findByUserIdOrderByWatchedDateDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private WatchLogResponse toResponse(WatchLog watchLog) {
        return new WatchLogResponse(
                watchLog.getId(),
                watchLog.getMovie().getId(),
                watchLog.getMovie().getTitle(),
                watchLog.getMovie().getReleaseYear(),
                watchLog.getMovie().getPosterImageUrl(),
                watchLog.getWatchedDate(),
                watchLog.getCreatedAt()
        );
    }
}
