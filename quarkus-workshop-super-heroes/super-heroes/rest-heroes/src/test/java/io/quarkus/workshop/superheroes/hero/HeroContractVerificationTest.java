package io.quarkus.workshop.superheroes.hero;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@QuarkusTest
@Provider("rest-heroes")
@PactFolder("pacts")
public class HeroContractVerificationTest {
    private static final String NO_RANDOM_HERO_FOUND_STATE = "No random hero found";

    @ConfigProperty(name = "quarkus.http.test-port")
    int quarkusPort;


    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void beforeEach(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", this.quarkusPort));

        // Have to do this here because the CDI context doesn't seem to be available
        // in the @State method below
        var isNoRandomHeroFoundState = Optional.ofNullable(context.getInteraction().getProviderStates())
            .orElseGet(List::of)
            .stream()
            .filter(state -> NO_RANDOM_HERO_FOUND_STATE.equals(state.getName()))
            .count() > 0;

        if (isNoRandomHeroFoundState) {
            PanacheMock.mock(Hero.class);
            when(Hero.findRandom())
                .thenReturn(Uni.createFrom().nullItem());

        }
    }


    @State(NO_RANDOM_HERO_FOUND_STATE)
    public void clearData() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Already handled in beforeEach
        // See https://github.com/quarkusio/quarkus/issues/22611
    }

}


