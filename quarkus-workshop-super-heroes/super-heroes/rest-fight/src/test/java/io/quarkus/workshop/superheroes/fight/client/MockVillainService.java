// tag::adocTest[]
package io.quarkus.workshop.superheroes.fight.client;

// end::adocTest[]
import io.quarkus.test.Mock;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;

// tag::adocTest[]
@Mock
@ApplicationScoped
@RestClient
public class MockVillainService implements VillainService {

    public static final String DEFAULT_VILLAIN_NAME = "Super Chocolatine";
    public static final String DEFAULT_VILLAIN_PICTURE = "super_chocolatine.png";
    public static final String DEFAULT_VILLAIN_POWERS = "does not eat pain au chocolat";
    public static final int DEFAULT_VILLAIN_LEVEL = 42;

    @Override
    public Villain findRandomVillain() {
        Villain villain = new Villain();
        villain.setName(DEFAULT_VILLAIN_NAME);
        villain.setPicture(DEFAULT_VILLAIN_PICTURE);
        villain.setPowers(DEFAULT_VILLAIN_POWERS);
        villain.setLevel(DEFAULT_VILLAIN_LEVEL);
        return villain;
    }
}
// end::adocTest[]
