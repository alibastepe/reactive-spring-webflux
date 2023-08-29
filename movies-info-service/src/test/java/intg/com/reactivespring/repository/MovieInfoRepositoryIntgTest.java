package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")  //make sure to sue embedded mongo db
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll();
    }

    @Test
    void findAll() {

        var moviesInfoFlux =  movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        var movieInfo1 = new MovieInfo(null, "Batman Begins 2", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        var moviesInfoFlux =  movieInfoRepository.save(movieInfo1).log();

        StepVerifier.create(moviesInfoFlux)
                //.expectNextCount(3)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Batman Begins 2", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        var movieInfo  = movieInfoRepository.findById("abc").block();  //make sure movie is retrieved
        movieInfo.setYear(2021);

        var movieInfoMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(movieInfoMono)
                //.expectNextCount(3)
                .assertNext(movieInfo1 -> {

                    assertEquals(2021, movieInfo1.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        var movieInfo  = movieInfoRepository.deleteById("abc").block();  //make sure movie is deleted


        var movieInfoMono = movieInfoRepository.findAll();

        StepVerifier.create(movieInfoMono)
                .expectNextCount(2)


                .verifyComplete();
    }
}