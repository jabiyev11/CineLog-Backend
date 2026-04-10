package com.cinelog.controller;

import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.RatingRequest;
import com.cinelog.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/movies/{id}/rating")
    public MessageResponse rateMovie(@PathVariable Long id, @Valid @RequestBody RatingRequest request) {
        return ratingService.rateMovie(id, request);
    }

    @DeleteMapping("/movies/{id}/rating")
    public MessageResponse deleteRating(@PathVariable Long id) {
        return ratingService.deleteRating(id);
    }
}
