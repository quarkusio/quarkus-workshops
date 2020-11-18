package io.quarkus.workshop.superheroes.frontend;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;

@QuarkusTest
public class FrontendResourceTest {

    @Test
    public void testBasePathEndpoint() {
        given()
          .when().get("/api/frontend/basePath")
          .then()
             .statusCode(200)
             .body(is(instanceOf(String.class)));
    }

}
