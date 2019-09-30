package io.quarkus.workshop.superheroes.fight.health;

import io.quarkus.workshop.superheroes.fight.FightResource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

@Liveness
@ApplicationScoped
public class PingFightResourceHealthCheck implements HealthCheck {

    @Inject
    FightResource fightResource;

    @Override
    public HealthCheckResponse call() {
        Response response = fightResource.ping();
        return HealthCheckResponse.named("Ping Fight REST Endpoint").withData("Status code", response.getStatus()).up().build();
    }
}
