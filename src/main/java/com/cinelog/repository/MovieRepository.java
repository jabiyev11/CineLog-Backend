package com.cinelog.repository;

import com.cinelog.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("""
            select distinct m
            from Movie m
            left join m.directors d
            left join m.castMembers c
            where lower(m.title) like lower(concat('%', :query, '%'))
               or lower(d) like lower(concat('%', :query, '%'))
               or lower(c) like lower(concat('%', :query, '%'))
            order by m.title asc
            """)
    List<Movie> search(String query);
}
