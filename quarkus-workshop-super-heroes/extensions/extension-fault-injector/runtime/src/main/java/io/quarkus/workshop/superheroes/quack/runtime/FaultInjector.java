package io.quarkus.workshop.superheroes.quack.runtime;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

/**
 * This is a secret weapon from super villains!
 * <p>
 * A filter intercepting request and injecting faults and delays.
 */
public class FaultInjector implements Handler<RoutingContext> {

    private final Random random = new Random();

    private boolean enabled;
    private long delay;
    private double delayRatio;
    private double faultRatio;
    private String faultMessage;
    private int faultStatusCode;

    public FaultInjector setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public FaultInjector setDelay(long delay) {
        this.delay = delay;
        return this;
    }

    public FaultInjector setDelayRatio(double delayRatio) {
        this.delayRatio = delayRatio;
        return this;
    }

    public FaultInjector setFaultRatio(double faultRatio) {
        this.faultRatio = faultRatio;
        return this;
    }

    public FaultInjector setFaultMessage(String faultMessage) {
        this.faultMessage = faultMessage;
        return this;
    }

    public FaultInjector setFaultStatusCode(int faultStatusCode) {
        this.faultStatusCode = faultStatusCode;
        return this;
    }

    public FaultInjector validate() {
        if (delayRatio < 0 || delayRatio > 1) {
            throw new IllegalArgumentException("delayRatio must be between 0 and 1");
        }
        if (faultRatio < 0 || faultRatio > 1) {
            throw new IllegalArgumentException("delayRatio must be between 0 and 1");
        }
        return this;
    }

    /**
     * The interceptor logic.
     * @param rc the routing context
     */
    @Override
    public void handle(RoutingContext rc) {
        if (!enabled) {
            rc.next();
            return;
        }

        double rndFault = random.nextDouble();
        double rndDelay = random.nextDouble();

        rc.response().putHeader("X-quack", "quack quack quack");

        if (rndDelay < delayRatio) {
            rc.vertx().setTimer(delay, x -> {
                rc.response().putHeader("X-quack-delay", Long.toString(delay));
                injectFaultIfNeeded(rc, rndFault);
            });
        } else {
            injectFaultIfNeeded(rc, rndFault);
        }
    }

    private void injectFaultIfNeeded(RoutingContext rc, double rndFault) {
        if (rndFault < faultRatio) {
            rc.response().putHeader("X-quack-fault", "true");
            rc.response().setStatusCode(faultStatusCode).end(faultMessage);
        } else {
            rc.next();
        }
    }
}
