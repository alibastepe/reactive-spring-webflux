package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    static String MOVIES_INFO_URL = "/v1/movieinfos";

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        var movieInfo1 = new MovieInfo(null, "Batman Begins 1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo1)
                .exchange() //make the call to the endpoint
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void getAllMovieInfos() {
          webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange() //make the call to the endpoint
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoByYear() {
        var uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                .queryParam("year", 2005)
                .buildAndExpand()
                .toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange() //make the call to the endpoint
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfoById() {
        var movieInfoId = "abc";
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange() //make the call to the endpoint
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var retrievedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert retrievedMovieInfo != null;

                });
    }

    @Test
    void updateMovieInfo() {
        var movieInfo1 = new MovieInfo(null, "Dark Knight Rises 1", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL+ "/{id}", "abc")
                .bodyValue(movieInfo1)
                .exchange() //make the call to the endpoint
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert updatedMovieInfo != null;
                    assertNotNull(updatedMovieInfo.getMovieInfoId());
                    assertEquals("Dark Knight Rises 1", updatedMovieInfo.getName());
                });
    }

    @Test
    void deleteMovieInfo() {
        var movieInfoId = "abc";
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange() //make the call to the endpoint
                .expectStatus()
                .isNoContent();

    }

}