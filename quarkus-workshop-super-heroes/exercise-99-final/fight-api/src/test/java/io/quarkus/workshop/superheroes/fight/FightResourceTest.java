package io.quarkus.workshop.superheroes.fight;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.mapper.TypeRef;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Random;

import static io.quarkus.workshop.superheroes.fight.client.MockHeroService.*;
import static io.quarkus.workshop.superheroes.fight.client.MockVillainService.*;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FightResourceTest {

    private static final String DEFAULT_WINNER_NAME = "Super Baguette";
    private static final String DEFAULT_WINNER_PICTURE = "super_baguette.png";
    private static final int DEFAULT_WINNER_LEVEL = 42;
    private static final String DEFAULT_LOSER_NAME = "Super Chocolatine";
    private static final String DEFAULT_LOSER_PICTURE = "super_chocolatine.png";
    private static final int DEFAULT_LOSER_LEVEL = 6;

    private static final int NB_FIGHTS = 0;
    private static String fightId;

    @Container
    public static final PostgreSQLContainer DATABASE = new PostgreSQLContainer<>()
        .withDatabaseName("fights-database")
        .withUsername("superfight")
        .withPassword("superfight")
        .withExposedPorts(5432)
        .withCreateContainerCmdModifier(cmd ->
            cmd
                .withHostName("localhost")
                .withPortBindings(new PortBinding(Ports.Binding.bindPort(5432), new ExposedPort(5432)))
        );

    @Test
    void shouldNotGetUnknownFight() {
        Long randomId = new Random().nextLong();
        given()
            .pathParam("id", randomId)
            .when().get("/api/fights/{id}")
            .then()
            .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void shouldPingFightEndpoint() {
        given()
            .when().get("/api/fights/ping")
            .then()
            .statusCode(OK.getStatusCode())
            .body(is("ping fights"));
    }

    @Test
    @Order(1)
    void shouldGetInitialItems() {
        List<Fight> fights = get("/api/fights").then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .extract().body().as(getFightTypeRef());
        assertEquals(NB_FIGHTS, fights.size());
    }

    @Test
    void shouldGetRandomFighters() {
        given()
            .when().get("/api/fights/randomfighters")
            .then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .body("hero.name", Is.is(DEFAULT_HERO_NAME))
            .body("hero.picture", Is.is(DEFAULT_HERO_PICTURE))
            .body("hero.level", Is.is(DEFAULT_HERO_LEVEL))
            .body("villain.name", Is.is(DEFAULT_VILLAIN_NAME))
            .body("villain.picture", Is.is(DEFAULT_VILLAIN_PICTURE))
            .body("villain.level", Is.is(DEFAULT_VILLAIN_LEVEL));
    }

    @Test
    @Order(2)
    void shouldAddAnItem() {
        Fight fight = new Fight();
        fight.winnerName = DEFAULT_WINNER_NAME;
        fight.winnerPicture = DEFAULT_WINNER_PICTURE;
        fight.winnerLevel = DEFAULT_WINNER_LEVEL;
        fight.loserName = DEFAULT_LOSER_NAME;
        fight.loserPicture = DEFAULT_LOSER_PICTURE;
        fight.loserLevel = DEFAULT_LOSER_LEVEL;

        String location = given()
            .body(fight)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .post("/api/fights")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().header("Location");
        assertTrue(location.contains("/api/fights"));

        // Stores the id
        String[] segments = location.split("/");
        fightId = segments[segments.length - 1];
        assertNotNull(fightId);

        given()
            .pathParam("id", fightId)
            .when().get("/api/fights/{id}")
            .then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .body("winnerName", Is.is(DEFAULT_WINNER_NAME))
            .body("winnerPicture", Is.is(DEFAULT_WINNER_PICTURE))
            .body("winnerLevel", Is.is(DEFAULT_WINNER_LEVEL))
            .body("loserName", Is.is(DEFAULT_LOSER_NAME))
            .body("loserPicture", Is.is(DEFAULT_LOSER_PICTURE))
            .body("loserLevel", Is.is(DEFAULT_LOSER_LEVEL))
            .body("fightDate", Is.is(notNullValue()));

        List<Fight> fights = get("/api/fights").then()
            .statusCode(OK.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .extract().body().as(getFightTypeRef());
        assertEquals(NB_FIGHTS + 1, fights.size());
    }

    private TypeRef<List<Fight>> getFightTypeRef() {
        return new TypeRef<List<Fight>>() {
            // Kept empty on purpose
        };
    }
}
