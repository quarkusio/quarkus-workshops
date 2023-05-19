// tag::skeleton[]
package io.quarkus.workshop.superheroes.fight;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.workshop.superheroes.fight.client.DefaultTestHero;
import io.quarkus.workshop.superheroes.fight.client.DefaultTestVillain;
import io.quarkus.workshop.superheroes.fight.client.Hero;
import io.quarkus.workshop.superheroes.fight.client.Villain;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(
    providerName = "rest-heroes",
    pactVersion = PactSpecVersion.V4,
    hostInterface = "localhost",
    // Hard-code the Pact MockServer to what we defined in application.properties
    // I don't like it but couldn't figure out any other way without using dynamic config
    port = "8093"
)
public class FightResourceConsumerTest {

    private static final String HERO_API_BASE_URI = "/api/heroes";
    private static final String HERO_RANDOM_URI = HERO_API_BASE_URI + "/random";

    @Pact(consumer = "rest-fights")
    public V4Pact randomHeroFoundPact(PactDslWithProvider builder) {
        return builder
            .uponReceiving("A request for a random hero")
            .path(HERO_RANDOM_URI)
            .method(HttpMethod.GET)
            .headers(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .willRespondWith()
            .status(Response.Status.OK.getStatusCode())
            .headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .body(newJsonBody(body ->
                    body
                        .stringType("name", DefaultTestHero.DEFAULT_HERO_NAME)
                        .integerType("level", DefaultTestHero.DEFAULT_HERO_LEVEL)
                        .stringType("picture", DefaultTestHero.DEFAULT_HERO_PICTURE)
                ).build()
            )
            .toPact(V4Pact.class);
    }
    // end::skeleton[]

    // tag::randomHeroFound[]
    @Test
    @PactTestFor(pactMethod = "randomHeroFoundPact")
    void randomHeroFound() {
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

        // We're really trying to test the fighter, so we want to make sure it still passes back
        // a villain
        Villain villain = fighters.villain;
        assertEquals(villain.name, DefaultTestVillain.DEFAULT_VILLAIN_NAME);
        assertEquals(villain.picture, DefaultTestVillain.DEFAULT_VILLAIN_PICTURE);
        assertEquals(villain.level, DefaultTestVillain.DEFAULT_VILLAIN_LEVEL);
    }
    // end::randomHeroFound[]

    // tag::randomHeroNotFound[]
    @Pact(consumer = "rest-fights")
    public V4Pact randomHeroNotFoundPact(PactDslWithProvider builder) {
        return builder
            .given("No random hero found")
            .uponReceiving("A request for a random hero")
            .path(HERO_RANDOM_URI)
            .method(HttpMethod.GET)
            .headers(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .willRespondWith()
            .status(Status.NOT_FOUND.getStatusCode())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "randomHeroNotFoundPact")
    void shouldGetRandomFighters() {
        given()
            .when()
            .get("/api/fights/randomfighters")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("hero.name", Is.is("Fallback hero"))
            .body("hero.picture",
                Is.is("https://dummyimage.com/280x380/1e8fff/ffffff&text=Fallback+Hero"))
            .body("hero.level", Is.is(1));
    }
    // end::randomHeroNotFound[]
}
