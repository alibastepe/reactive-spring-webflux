package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    static String MOVIES_INFO_URL = "/v1/movieinfos";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoServiceMock;

    @Test
    void getAllMovieInfos()  {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        Mockito.when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));


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
    void addMovieInfo() {
        var movieInfo1 = new MovieInfo(null, "Batman Begins 1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        Mockito.when(movieInfoServiceMock.addMovie(isA(MovieInfo.class))).thenReturn(Mono.just(new MovieInfo("mockId", "Batman Begins 1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));
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
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo_validation() {
        var movieInfo1 = new MovieInfo(null, "Batman Begins 1", -2005, List.of(""), LocalDate.parse("2005-06-15"));

        Mockito.when(movieInfoServiceMock.addMovie(isA(MovieInfo.class))).thenReturn(Mono.just(new MovieInfo("mockId", "Batman Begins 1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo1)
                .exchange() //make the call to the endpoint
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.println("response body: " + responseBody);
                    assert  responseBody != null;
                });
    }
}
