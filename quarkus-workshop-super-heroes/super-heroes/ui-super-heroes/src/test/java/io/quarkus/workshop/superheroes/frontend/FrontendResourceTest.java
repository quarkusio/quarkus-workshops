package io.quarkus.workshop.superheroes.frontend;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class FrontendResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/api/frontend")
          .then()
             .statusCode(200)
             .body(is("hello"));
    }

}