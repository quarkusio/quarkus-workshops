package io.quarkus.workshop.superheroes.fight;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class ApplicationLifeCycle {

    private static final Logger LOGGER = Logger.getLogger(ApplicationLifeCycle.class);

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application FIGHT is starting with profile " + ProfileManager.getActiveProfile());
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("The application FIGHT is stopping...");
    }
}
