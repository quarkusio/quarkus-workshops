package io.quarkus.workshop.superheroes.banner.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "banner", phase = ConfigPhase.BUILD_TIME)
public class BannerConfig {

    /**
     * The path of the banner.
     */
    @ConfigItem public String path;
}
