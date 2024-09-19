package io.quarkus.workshop.superheroes.version.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.version")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface VersionConfig {

    /**
     * Enables or disables the version printing at startup.
     */
    @WithDefault("true")
    boolean enabled();
}
