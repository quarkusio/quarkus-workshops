package io.quarkus.workshop.superheroes.fight.health;

import io.quarkus.workshop.superheroes.fight.FightResource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class PingFightResourceHealthCheck implements HealthCheck {

    @Inject
    FightResource fightResource;

    @Override
    public HealthCheckResponse call() {
        fightResource.hello();
        return HealthCheckResponse.named("Ping Fight REST Endpoint").up().build();
    }
}
