package io.quarkus.workshop.superheroes.quack.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.vertx.http.deployment.VertxWebRouterBuildItem;
import io.quarkus.workshop.superheroes.quack.runtime.FaultInjectorRecorder;
import io.quarkus.workshop.superheroes.quack.runtime.QuackConfig;

/**
 * The Quack /. Fault injector processor.
 */
public class QuackProcessor {

    /**
     * Configures the fault injection and registers the route.
     *
     * @param feature  to produce a feature build item
     * @param recorder the recorder executing the initialization
     * @param config   the config
     * @param router   the router on which the faults are going to be injected
     */
    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void initialize(BuildProducer<FeatureBuildItem> feature,
        FaultInjectorRecorder recorder,
        QuackConfig config,
        VertxWebRouterBuildItem router) {
        feature.produce(new FeatureBuildItem("quack"));
        recorder.configure(config, router.getRouter());
    }
}
