package com.cinelog.controller;

import com.cinelog.dto.MovieDetailResponse;
import com.cinelog.dto.MovieRequest;
import com.cinelog.dto.MovieSummaryResponse;
import com.cinelog.dto.PageResponse;
import com.cinelog.dto.SearchResponse;
import com.cinelog.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/movies")
    public PageResponse<MovieSummaryResponse> getMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        return movieService.getMovies(pageable);
    }

    @GetMapping("/movies/{id}")
    public MovieDetailResponse getMovie(@PathVariable Long id) {
        return movieService.getMovie(id);
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam("q") String query) {
        return movieService.searchMovies(query);
    }

    @PostMapping("/admin/movies")
    @ResponseStatus(HttpStatus.CREATED)
    public MovieDetailResponse createMovie(@Valid @RequestBody MovieRequest request) {
        return movieService.createMovie(request);
    }

    @PutMapping("/admin/movies/{id}")
    public MovieDetailResponse updateMovie(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        return movieService.updateMovie(id, request);
    }

    @DeleteMapping("/admin/movies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }
}
