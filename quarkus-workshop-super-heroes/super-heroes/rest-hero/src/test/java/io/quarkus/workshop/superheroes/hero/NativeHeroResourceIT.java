// tag::adocResourceIT[]
package io.quarkus.workshop.superheroes.hero;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.NativeImageTest;
import io.restassured.common.mapper.TypeRef;
import io.vertx.core.json.JsonObject;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@NativeImageTest
@QuarkusTestResource(DatabaseResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NativeHeroResourceIT {

    private static final String DEFAULT_NAME = "Super Baguette";
    private static final String UPDATED_NAME = "Super Baguette (updated)";
    private static final String DEFAULT_OTHER_NAME = "Super Baguette Tradition";
    private static final String UPDATED_OTHER_NAME = "Super Baguette Tradition (updated)";
    private static final String DEFAULT_PICTURE = "super_baguette.png";
    private static final String UPDATED_PICTURE = "super_baguette_updated.png";
    private static final String DEFAULT_POWERS = "eats baguette really quickly";
    private static final String UPDATED_POWERS = "eats baguette really quickly (updated)";

    private static String heroId;

    // tag::adocOpenAPI[]
    @Test
    void shouldPingOpenAPI() {
        given()
            .header(ACCEPT, APPLICATION_JSON)
            .when().get("/openapi")
            .then()
            .statusCode(OK.getStatusCode());
    }

    // end::adocOpenAPI[]

    // tag::adocHealth[]
    @Test
    void shouldPingLiveness() {
        given()
            .when().get("/health/live")
            .then()
            .statusCode(OK.getStatusCode());
    }

    @Test
    void shouldPingReadiness() {
        given()
            .when().get("/health/ready")
            .then()
            .statusCode(OK.getStatusCode());
    }
    // end::adocHealth[]

    // tag::adocMetrics[]
    @Test
    void shouldPingMetrics() {
        given()
            .header(ACCEPT, APPLICATION_JSON)
            .when().get("/metrics/application")
            .then()
            .statusCode(OK.getStatusCode());
    }
    // end::adocMetrics[]

    @Test
    public void testHelloEndpoint() {
        given()
            .when().get("/api/heroes/hello")
            .then()
            .statusCode(200)
            .body(is("hello"));
    }

    @Test
    void shouldNotGetUnknownHero() {
        Long randomId = new Random().nextLong();
        given()
            .pathParam("id", randomId)
            .when().get("/api/heroes/{id}")
            .then()
            .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void shouldNotAddInvalidItem() {
        JsonObject hero = new JsonObject();
        hero.put("otherName", DEFAULT_OTHER_NAME);
        hero.put("picture", DEFAULT_PICTURE);
        hero.put("powers", DEFAULT_POWERS);
        hero.put("level", 0);

        given()
            .body(hero.encode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .post("/api/heroes")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @Order(1)
    void shouldGetInitialItems() {
        List<Hero> heroes = get("/api/heroes").then()
            .statusCode(OK.getStatusCode())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .extract().body().as(getHeroTypeRef());
        assertEquals(0, heroes.size());
    }

    @Test
    @Order(2)
    void shouldAddAnItem() {
        JsonObject hero = new JsonObject();
        hero.put("name", DEFAULT_NAME);
        hero.put("otherName", DEFAULT_OTHER_NAME);
        hero.put("picture", DEFAULT_PICTURE);
        hero.put("powers", DEFAULT_POWERS);
        hero.put("level", 20);

        String location = given()
            .body(hero.encode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .post("/api/heroes")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().header("Location");
        assertTrue(location.contains("/api/heroes"));

        // Stores the id
        String[] segments = location.split("/");
        heroId = segments[segments.length - 1];
        assertNotNull(heroId);

        given()
            .pathParam("id", heroId)
            .when().get("/api/heroes/{id}")
            .then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .body("name", Is.is(DEFAULT_NAME))
            .body("otherName", Is.is(DEFAULT_OTHER_NAME))
            .body("level", Is.is(60))
            .body("picture", Is.is(DEFAULT_PICTURE))
            .body("powers", Is.is(DEFAULT_POWERS));

        List<Hero> heroes = get("/api/heroes").then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .extract().body().as(getHeroTypeRef());
        assertEquals(1, heroes.size());
    }

    @Test
    @Order(3)
    void shouldUpdateAnItem() {
        JsonObject hero = new JsonObject();
        hero.put("id", Long.valueOf(heroId));
        hero.put("name", UPDATED_NAME);
        hero.put("otherName", UPDATED_OTHER_NAME);
        hero.put("picture", UPDATED_PICTURE);
        hero.put("powers", UPDATED_POWERS);
        hero.put("level", 21);

        given()
            .body(hero.encode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .put("/api/heroes")
            .then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .body("name", Is.is(UPDATED_NAME))
            .body("otherName", Is.is(UPDATED_OTHER_NAME))
            .body("level", Is.is(21))
            .body("picture", Is.is(UPDATED_PICTURE))
            .body("powers", Is.is(UPDATED_POWERS));

        List<Hero> heroes = get("/api/heroes").then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .extract().body().as(getHeroTypeRef());
        assertEquals(1, heroes.size());
    }

    @Test
    @Order(4)
    void shouldRemoveAnItem() {
        given()
            .pathParam("id", heroId)
            .when().delete("/api/heroes/{id}")
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        List<Hero> heroes = get("/api/heroes").then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .extract().body().as(getHeroTypeRef());
        assertEquals(0, heroes.size());
    }

    private TypeRef<List<Hero>> getHeroTypeRef() {
        return new TypeRef<List<Hero>>() {
            // Kept empty on purpose
        };
    }

}
// end::adocResourceIT[]
