package io.quarkus.workshop.superheroes.fight;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.workshop.superheroes.fight.client.*;
import io.restassured.common.mapper.TypeRef;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FightResourceTest {

    private static final String DEFAULT_WINNER_NAME = "Super Baguette";
    private static final String DEFAULT_WINNER_PICTURE = "super_baguette.png";
    private static final int DEFAULT_WINNER_LEVEL = 42;
    private static final String DEFAULT_WINNER_POWERS = "Eats baguette in less than a second";
    private static final String DEFAULT_LOSER_NAME = "Super Chocolatine";
    private static final String DEFAULT_LOSER_PICTURE = "super_chocolatine.png";
    private static final int DEFAULT_LOSER_LEVEL = 6;
    private static final String DEFAULT_LOSER_POWERS = "Transforms chocolatine into pain au chocolat";

    private static final int NB_FIGHTS = 3;
    private static String fightId;

    @InjectMock(convertScopes = true)
    @RestClient
    HeroProxy heroProxy;

    @BeforeEach
    public void setup() {
        when(heroProxy.findRandomHero()).thenReturn(DefaultTestHero.INSTANCE);
    }

    @Test
    void shouldPingOpenAPI() {
        given()
            .header(ACCEPT,
                APPLICATION_JSON)
            .when()
            .get("/q/openapi")
            .then()
            .statusCode(OK.getStatusCode());
    }

    @Test
    public void testHelloEndpoint() {
        given()
            .header(ACCEPT,
                TEXT_PLAIN)
            .when()
            .get("/api/fights/hello")
            .then()
            .statusCode(200)
            .body(is("Hello Fight Resource"));
    }

    @Test
    void shouldNotGetUnknownFight() {
        Long randomId = new Random().nextLong();
        given()
            .pathParam("id",
                randomId)
            .when()
            .get("/api/fights/{id}")
            .then()
            .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void shouldNotAddInvalidItem() {
        Fighters fighters = new Fighters();
        fighters.hero = null;
        fighters.villain = null;

        given().body(fighters)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .post("/api/fights")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @Order(1)
    void shouldGetInitialItems() {
        List<Fight> fights = get("/api/fights").then()
            .statusCode(OK.getStatusCode())
            .extract()
            .body()
            .as(getFightTypeRef());
        assertEquals(NB_FIGHTS, fights.size());
    }

    @Test
    @Order(2)
    void shouldAddAnItem() {
        Hero hero = new Hero();
        hero.name = DEFAULT_WINNER_NAME;
        hero.picture = DEFAULT_WINNER_PICTURE;
        hero.level = DEFAULT_WINNER_LEVEL;
        hero.powers = DEFAULT_WINNER_POWERS;
        Villain villain = new Villain();
        villain.name = DEFAULT_LOSER_NAME;
        villain.picture = DEFAULT_LOSER_PICTURE;
        villain.level = DEFAULT_LOSER_LEVEL;
        villain.powers = DEFAULT_LOSER_POWERS;
        Fighters fighters = new Fighters();
        fighters.hero = hero;
        fighters.villain = villain;

        fightId = given()
            .body(fighters)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .post("/api/fights")
            .then()
            .statusCode(OK.getStatusCode())
            .body(containsString("winner"),
                containsString("loser"))
            .extract()
            .body()
            .jsonPath()
            .getString("id");

        assertNotNull(fightId);

        given().pathParam("id", fightId)
            .when()
            .get("/api/fights/{id}")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("winnerName", Is.is(DEFAULT_WINNER_NAME))
            .body("winnerPicture", Is.is(DEFAULT_WINNER_PICTURE))
            .body("winnerLevel", Is.is(DEFAULT_WINNER_LEVEL))
            .body("winnerPowers", Is.is(DEFAULT_WINNER_POWERS))
            .body("loserName", Is.is(DEFAULT_LOSER_NAME))
            .body("loserPicture", Is.is(DEFAULT_LOSER_PICTURE))
            .body("loserLevel", Is.is(DEFAULT_LOSER_LEVEL))
            .body("loserPowers", Is.is(DEFAULT_LOSER_POWERS))
            .body("fightDate", Is.is(notNullValue()));

        List<Fight> fights = get("/api/fights").then()
            .statusCode(OK.getStatusCode())
            .extract()
            .body()
            .as(getFightTypeRef());
        assertEquals(NB_FIGHTS + 1, fights.size());
    }

    // tag::shouldGetRandomFighters[]
    @Test
    void shouldGetRandomFighters() {
        Fighters fighters = given()
            .when()
            .get("/api/fights/randomfighters")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract()
            .as(Fighters.class);

        Hero hero = fighters.hero;
        assertEquals(hero.name, DefaultTestHero.DEFAULT_HERO_NAME);
        assertEquals(hero.picture, DefaultTestHero.DEFAULT_HERO_PICTURE);
        assertEquals(hero.level, DefaultTestHero.DEFAULT_HERO_LEVEL);
        assertEquals(hero.powers, DefaultTestHero.DEFAULT_HERO_POWERS);

        Villain villain = fighters.villain;
        assertEquals(villain.name, DefaultTestVillain.DEFAULT_VILLAIN_NAME);
        assertEquals(villain.picture, DefaultTestVillain.DEFAULT_VILLAIN_PICTURE);
        assertEquals(villain.level, DefaultTestVillain.DEFAULT_VILLAIN_LEVEL);
        assertEquals(villain.powers, DefaultTestVillain.DEFAULT_VILLAIN_POWERS);
    }
    // end::shouldGetRandomFighters[]

    @Test
    void shouldNarrate() {
        Fight fight = new Fight();
        fight.fightDate = Instant.now();
        fight.winnerName = DEFAULT_WINNER_NAME;
        fight.winnerLevel = DEFAULT_WINNER_LEVEL;
        fight.winnerPowers = DEFAULT_WINNER_POWERS;
        fight.winnerPicture = DEFAULT_WINNER_PICTURE;
        fight.loserName = DEFAULT_LOSER_NAME;
        fight.loserLevel = DEFAULT_LOSER_LEVEL;
        fight.loserPowers = DEFAULT_LOSER_POWERS;
        fight.loserPicture = DEFAULT_LOSER_PICTURE;
        fight.winnerTeam = "villains";
        fight.loserTeam = "heroes";

        given().body(fight)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, TEXT_PLAIN)
            .when()
            .post("/api/fights/narrate")
            .then()
            .statusCode(CREATED.getStatusCode());
    }

    private TypeRef<List<Fight>> getFightTypeRef() {
        return new TypeRef<List<Fight>>() {
            // Kept empty on purpose
        };
    }
}
