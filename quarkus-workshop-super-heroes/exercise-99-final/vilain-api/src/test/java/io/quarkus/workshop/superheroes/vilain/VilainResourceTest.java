package io.quarkus.workshop.superheroes.vilain;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class VilainResourceTest {

    @Test
    public void testHeroEndpoint() {
        given()
          .when().get("/vilain")
          .then()
             .statusCode(200)
             .body(is("hello vilain"));
    }

}
