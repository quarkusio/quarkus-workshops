package io.quarkus.workshop.superheroes.ui;

import io.quarkiverse.quinoa.testing.QuinoaTestProfiles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static io.restassured.RestAssured.get;
import static io.restassured.http.ContentType.HTML;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.matchesPattern;

/**
 * Tests the served javascript application.
 * By default, the Web UI is not build/served in @QuarkusTest. The goal is to be able to test
 * your api without having to wait for the Web UI build.
 * The `Enable` test profile enables the Web UI (build and serve).
 */
@QuarkusTest
@TestProfile(QuinoaTestProfiles.EnableAndRunTests.class)
public class WebUITests {

    @Test
    public void webApplicationEndpoint() {
        get("/")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(HTML)
            .body(matchesPattern(Pattern.compile(".*<div id=\"root\">.*", Pattern.DOTALL)));
        // We don't want to do full HTML parsing here, because that should
        // be in the javascript tests. Instead, just confirm that we
        // are successfully serving the HTML content on /
    }
}
