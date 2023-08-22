package io.quarkus.workshop.superheroes.narration;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.*;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class NarrationResourceTest {

    private static final String HERO_NAME = "Super Baguette";
    private static final int HERO_LEVEL = 42;
    private static final String HERO_POWERS = "Eats baguette in less than a second";
    private static final String VILLAIN_NAME = "Super Chocolatine";
    private static final int VILLAIN_LEVEL = 43;
    private static final String VILLAIN_POWERS = "Transforms chocolatine into pain au chocolat";


    @BeforeEach
//    public void setup() {
//        Mockito.when(heroProxy.findRandomHero()).thenReturn(DefaultTestHero.INSTANCE);
//    }

    @Test
    void shouldPingOpenAPI() {
        given()
            .header(ACCEPT, APPLICATION_JSON)
            .when().get("/q/openapi")
            .then()
            .statusCode(OK.getStatusCode());
    }

    @Test
    public void testHelloEndpoint() {
        given()
            .when().get("/api/narration/hello")
            .then()
            .statusCode(200)
            .body(is("Hello Narration Resource"));
    }

    @Test
    void shouldNarrateAFight() {
        Fight fight = new Fight();
        fight.winnerName = VILLAIN_NAME;
        fight.winnerLevel = VILLAIN_LEVEL;
        fight.winnerPowers = VILLAIN_POWERS;
        fight.loserName = HERO_NAME;
        fight.loserLevel = HERO_LEVEL;
        fight.loserPowers = HERO_POWERS;
        fight.winnerTeam = "villains";
        fight.loserTeam = "heroes";

        String narration = given().log().all()
            .body(fight)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, TEXT_PLAIN)
            .when()
            .post("/api/narration")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().body().as(String.class);
    }
}
