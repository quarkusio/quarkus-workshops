package io.quarkus.workshop.superheroes.vilain;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class VillainResourceTest {

    @Test
    public void shouldNotGetRandomVilain() {
        String uuid = UUID.randomUUID().toString();
        given()
                .pathParam("name", uuid)
                .when().get("/api/villains/{name}")
                .then()
                .statusCode(200);
    }

    @Test
    public void shouldPingVilainEndpoint() {
        given()
          .when().get("/api/villains/ping")
          .then()
             .statusCode(200)
             .body(is("ping villains"));
    }

}
