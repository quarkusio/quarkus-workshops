package io.quarkus.workshop.superheroes.fight;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class FightResourceTest {

    @Test
    public void testFightEndpoint() {
        given()
                .when().get("/fight")
                .then()
                .statusCode(200)
                .body(is("hello fight"));
    }

}
