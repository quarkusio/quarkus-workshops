package io.quarkus.workshop.superheroes.vilain;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.mapper.TypeRef;
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
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
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

    private TypeRef<List<Villain>> getVillainTypeRef() {
        return new TypeRef<List<Villain>>() {
            // Kept empty on purpose
        };
    }
}
