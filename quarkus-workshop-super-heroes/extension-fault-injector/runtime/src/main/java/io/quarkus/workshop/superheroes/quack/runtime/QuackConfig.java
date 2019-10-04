package io.quarkus.workshop.superheroes.quack.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.time.Duration;
import java.util.Optional;

/**
 * Configure the fault injector extension, aka the quack extension.
 * <p>
 * Note: this extension is a secret weapon used by the super villains.
 */
@ConfigRoot(name = "fault", phase = ConfigPhase.RUN_TIME)
public class QuackConfig {

    /**
     * Enables / Disables the extension.
     */
    @ConfigItem(defaultValue = "false")
    public boolean enabled;

    /**
     * Delay added to the response.
     * Defaults is 1 second.
     */
    @ConfigItem
    public Optional<Duration> delay;

    /**
     * Ratio (between 0 and 1) of request impacted by the delay.
     * 1 means that all the request are delayed. 0 means none.
     * Defaults is 0.33.
     */
    @ConfigItem(defaultValue = "0.33")
    public double delayRatio;

    /**
     * Ratio (between 0 and 1) of request impacted by a fault injection.
     * 1 means that all the request are faulty. 0 means none.
     * Defaults is 0.33.
     */
    @ConfigItem(defaultValue = "0.33")
    public double faultRatio;

    /**
     * Configures the fault message.
     */
    @ConfigItem(defaultValue = "quack quack quack")
    public String faultMessage;

    /**
     * Configure the fault status code.
     */
    @ConfigItem(defaultValue = "500")
    public int faultStatusCode;

}
