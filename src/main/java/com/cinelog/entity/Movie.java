package com.cinelog.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int releaseYear;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_directors", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "director", nullable = false)
    private List<String> directors = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_cast_members", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "cast_member", nullable = false)
    private List<String> castMembers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre", nullable = false)
    private List<String> genres = new ArrayList<>();

    @Column(nullable = false)
    private int durationMinutes;

    private String country;

    private String language;

    @Column(length = 4000)
    private String synopsis;

    private String posterImageUrl;
}
