package io.quarkus.workshop.superheroes.quack.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.ext.web.Router;

import java.time.Duration;

/**
 * Records the configuration and register the injector.
 */
@Recorder
public class FaultInjectorRecorder {

    public void configure(QuackConfig config, RuntimeValue<Router> router) {
        FaultInjector injector = new FaultInjector()
            .setEnabled(config.enabled)
            .setDelay(config.delay.orElse(Duration.ofSeconds(1)).toMillis())
            .setDelayRatio(config.delayRatio)
            .setFaultMessage(config.faultMessage)
            .setFaultRatio(config.faultRatio)
            .setFaultStatusCode(config.faultStatusCode)
            .validate();

        // Register the route, the -1 indicate it will be called first.
        router.getValue().route().order(-1).handler(injector);
    }
}
