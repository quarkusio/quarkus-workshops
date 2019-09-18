package io.quarkus.workshop.superheroes.hero;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HeroResourceTest {

    @Container
    public static final PostgreSQLContainer DATABASE = new PostgreSQLContainer<>()
        .withDatabaseName("heroes-database")
        .withUsername("superman")
        .withPassword("superman")
        .withExposedPorts(5432)
        .withCreateContainerCmdModifier(cmd ->
            cmd
                .withHostName("localhost")
                .withPortBindings(new PortBinding(Ports.Binding.bindPort(5432), new ExposedPort(5432)))
        );

    @Test
    public void shouldNotGetRandomHero() {
        String uuid = UUID.randomUUID().toString();
        given()
            .pathParam("name", uuid)
            .when().get("/api/heroes/{name}")
            .then()
            .statusCode(204);
    }

    @Test
    public void shouldPingHeroEndpoint() {
        given()
            .when().get("/api/heroes/ping")
            .then()
            .statusCode(200)
            .body(is("ping heroes"));
    }

}
