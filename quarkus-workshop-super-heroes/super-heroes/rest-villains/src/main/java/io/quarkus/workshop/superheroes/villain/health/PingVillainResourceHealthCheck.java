package io.quarkus.workshop.superheroes.villain.health;

import io.quarkus.workshop.superheroes.villain.VillainResource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.inject.Inject;

@Liveness
public class PingVillainResourceHealthCheck implements HealthCheck {

    @Inject
    VillainResource villainResource;

    @Override
    public HealthCheckResponse call() {
        String response = villainResource.hello();
        return HealthCheckResponse.named("Ping Villain REST Endpoint").withData("Response", response).up().build();
    }
}
