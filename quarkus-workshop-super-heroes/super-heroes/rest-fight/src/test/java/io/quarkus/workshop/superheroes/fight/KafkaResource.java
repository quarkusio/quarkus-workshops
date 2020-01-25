package io.quarkus.workshop.superheroes.fight;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.KafkaContainer;

import java.util.Collections;
import java.util.Map;

public class KafkaResource implements QuarkusTestResourceLifecycleManager  {

    private static final KafkaContainer KAFKA = new KafkaContainer();


    @Override
    public Map<String, String> start() {
        KAFKA.start();
        return Collections.singletonMap("kafka.bootstrap.servers", KAFKA.getBootstrapServers());
    }

    @Override
    public void stop() {
        KAFKA.stop();
    }
}
