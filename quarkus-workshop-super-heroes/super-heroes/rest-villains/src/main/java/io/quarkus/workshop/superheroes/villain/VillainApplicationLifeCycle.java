package io.quarkus.workshop.superheroes.villain;

import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

// tag::adocJavadoc[]

/**
 * Demonstrates how to use Quarkus {@link StartupEvent}s and {@link ShutdownEvent}s as well as how to gain access to the {@link ConfigUtils}.
 */
// end::adocJavadoc[]
@ApplicationScoped
public class VillainApplicationLifeCycle {


    void onStart(@Observes StartupEvent ev) {
        Log.info(" __     ___ _ _       _             _    ____ ___ ");
        Log.info(" \\ \\   / (_) | | __ _(_)_ __       / \\  |  _ \\_ _|");
        Log.info("  \\ \\ / /| | | |/ _` | | '_ \\     / _ \\ | |_) | | ");
        Log.info("   \\ V / | | | | (_| | | | | |   / ___ \\|  __/| | ");
        Log.info("    \\_/  |_|_|_|\\__,_|_|_| |_|  /_/   \\_\\_|  |___|");
        // tag::adocProfile[]
        Log.info("The application VILLAIN is starting with profile " + ConfigUtils.getProfiles());
        // end::adocProfile[]
    }

    void onStop(@Observes ShutdownEvent ev) {
        Log.info("The application VILLAIN is stopping...");
    }
}
