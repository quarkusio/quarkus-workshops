package io.quarkus.workshop.superheroes.quack.deployment;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import io.vertx.ext.web.Router;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class QuackProcessorTest {

    @SuppressWarnings("unused")
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
        .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
            .addClass(MyResource.class)
            .addAsResource("injection-config.properties", "application.properties"));

    @Test
    public void test() {
        List<Response> responses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            responses.add(RestAssured.get("/").thenReturn());
        }

        responses.forEach(response -> Assertions.assertNotNull(response.getHeader("X-quack")));
        Assertions.assertTrue(responses.stream().map(ResponseOptions::statusCode).anyMatch(s -> s == 418));
        Assertions.assertTrue(responses.stream().anyMatch(r -> r.getHeader("X-quack-delay") != null));
        Assertions.assertTrue(responses.stream().anyMatch(r -> r.getHeader("X-quack-fault") != null));

    }

    @ApplicationScoped
    public static class MyResource {

        public void init(@Observes Router router) {
            router.route("/").handler(rc -> rc.response().end("hello"));
        }

    }
}
