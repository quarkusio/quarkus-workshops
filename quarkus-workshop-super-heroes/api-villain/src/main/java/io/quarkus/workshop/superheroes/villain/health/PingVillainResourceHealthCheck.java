package io.quarkus.workshop.superheroes.villain.health;

import io.quarkus.workshop.superheroes.villain.VillainResource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class PingVillainResourceHealthCheck implements HealthCheck {

    @Inject
    VillainResource villainResource;

    @Override
    public HealthCheckResponse call() {
        villainResource.hello();
        return HealthCheckResponse.named("Ping Villain REST Endpoint").up().build();
    }
}
