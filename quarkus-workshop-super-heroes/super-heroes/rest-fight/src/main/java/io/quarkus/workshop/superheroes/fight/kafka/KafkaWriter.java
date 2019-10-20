// tag::adocWriter[]
package io.quarkus.workshop.superheroes.fight.kafka;

import io.quarkus.workshop.superheroes.fight.Fight;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

@ApplicationScoped
public class KafkaWriter {

    private static final Logger LOGGER = Logger.getLogger(KafkaWriter.class);
    private Jsonb jsonb;

    @PostConstruct
    public void init() {
        jsonb = JsonbBuilder.create();
    }

    @PreDestroy
    public void cleanup() {
        try {
            jsonb.close();
        } catch (Exception e) {
            LOGGER.warn("Unable to close JSON-B: ", e);
        }
    }

    @Incoming("fights-channel")
    @Outgoing("fights")
    public String toJson(Fight fight) {
        return jsonb.toJson(fight);
    }

}
// end::adocWriter[]
