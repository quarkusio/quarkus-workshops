// tag::adocPingHero[]
package io.quarkus.workshop.superheroes.hero.health;

import io.quarkus.workshop.superheroes.hero.HeroResource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class PingHeroResourceHealthCheck implements HealthCheck {

    @Inject
    HeroResource heroResource;

    @Override
    public HealthCheckResponse call() {
        heroResource.hello();
        return HealthCheckResponse.named("Ping Hero REST Endpoint").up().build();
    }
}
// end::adocPingHero[]
