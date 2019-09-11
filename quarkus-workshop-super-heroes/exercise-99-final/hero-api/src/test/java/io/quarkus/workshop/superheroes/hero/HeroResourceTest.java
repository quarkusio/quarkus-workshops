package io.quarkus.workshop.superheroes.hero;

import io.quarkus.test.junit.QuarkusTest;

    
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class HeroResourceTest {

    @Test
    public void shouldNotGetRandomHero() {
        String uuid = UUID.randomUUID().toString();
        given()
                .pathParam("name", uuid)
                .when().get("/api/heroes/{name}")
                .then()
                .statusCode(200);
    }

    @Test
    public void shouldPingHeroEndpoint() {
        given()
          .when().get("/api/heroes/ping")
          .then()
             .statusCode(200)
             .body(is("ping heroes"));
    }

}
