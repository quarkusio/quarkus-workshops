package io.quarkus.workshop.superheroes.banner.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "banner", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class BannerConfig {

    /**
     * The path of the banner.
     */
    @ConfigItem public String file;
}
