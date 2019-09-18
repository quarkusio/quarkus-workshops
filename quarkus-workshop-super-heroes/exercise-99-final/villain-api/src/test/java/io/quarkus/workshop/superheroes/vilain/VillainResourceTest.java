package io.quarkus.workshop.superheroes.vilain;

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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.*;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VillainResourceTest {

    private static final int NB_VILLAINS = 581;

    @Container
    public static final PostgreSQLContainer DATABASE = new PostgreSQLContainer<>()
            .withDatabaseName("villains-database")
            .withUsername("superbad")
            .withPassword("superbad")
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd ->
                    cmd
                            .withHostName("localhost")
                            .withPortBindings(new PortBinding(Ports.Binding.bindPort(5432), new ExposedPort(5432)))
            );

    @Test
    void shouldNotGetRandomVillain() {
        String uuid = UUID.randomUUID().toString();
        given()
                .pathParam("name", uuid)
                .when().get("/api/villains/{name}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void shouldPingVillainEndpoint() {
        given()
                .when().get("/api/villains/ping")
                .then()
                .statusCode(OK.getStatusCode())
                .body(is("ping villains"));
    }

    @Test
    @Order(1)
    void shouldGetInitialItems() {
        List<Villain> villains = get("/api/villains").then()
            .statusCode(OK.getStatusCode())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .extract().body().as(getVillainTypeRef());
        assertEquals(NB_VILLAINS, villains.size());
    }

    @Test
    @Order(2)
    void shouldAddAnItem() {
        Villain villain = new Villain();
        villain.name = "Super Chocolatine";
        villain.level = 42;
        given()
            .body(villain)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .when()
            .post("/api/villains")
            .then()
            .statusCode(CREATED.getStatusCode())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .body("name", Is.is("Super Chocolatine"))
            .body("level", Is.is(42));

        List<Villain> heroes = get("/api/villains").then()
            .statusCode(OK.getStatusCode())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .extract().body().as(getVillainTypeRef());
        assertEquals(NB_VILLAINS + 1, heroes.size());
    }

    @Test
    @Order(3)
    void testUpdatingAnItem() {
        Villain villain = new Villain();
        villain.name = "Super Chocolatine (updated)";
        villain.level = 42;
        given()
            .body(villain)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .pathParam("id", 5)
            .when()
            .patch("/api/{id}")
            .then()
            .statusCode(OK.getStatusCode())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .body("name", Is.is(villain.name))
            .body("level", Is.is(42));
    }

    private TypeRef<List<Villain>> getVillainTypeRef() {
        return new TypeRef<List<Villain>>() {
            // Kept empty on purpose
        };
    }
}
