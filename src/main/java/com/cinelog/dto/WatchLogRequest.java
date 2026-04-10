package com.cinelog.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WatchLogRequest(@NotNull LocalDate watchedDate) {
}
