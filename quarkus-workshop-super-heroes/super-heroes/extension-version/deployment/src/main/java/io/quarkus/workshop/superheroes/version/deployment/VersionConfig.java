package io.quarkus.workshop.superheroes.version.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "version")
public class VersionConfig {

    /**
     * Enables or disables the version printing at startup.
     */
    @ConfigItem(defaultValue = "true")
    boolean enabled;
}
