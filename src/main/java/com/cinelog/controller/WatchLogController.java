package com.cinelog.controller;

import com.cinelog.dto.WatchLogRequest;
import com.cinelog.dto.WatchLogResponse;
import com.cinelog.service.WatchLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WatchLogController {

    private final WatchLogService watchLogService;

    @PostMapping("/watchlog/{movieId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WatchLogResponse logWatch(@PathVariable Long movieId, @Valid @RequestBody WatchLogRequest request) {
        return watchLogService.logWatch(movieId, request);
    }

    @GetMapping("/watchlog")
    public List<WatchLogResponse> getWatchHistory() {
        return watchLogService.getWatchHistory();
    }
}
