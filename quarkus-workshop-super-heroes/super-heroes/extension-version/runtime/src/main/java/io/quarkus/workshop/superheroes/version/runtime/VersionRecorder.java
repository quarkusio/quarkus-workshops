package io.quarkus.workshop.superheroes.version.runtime;

import io.quarkus.runtime.annotations.Recorder;
import org.jboss.logging.Logger;

@Recorder
public class VersionRecorder {

    public void printVersion(VersionConfig versionConfig, String version) {
        if (versionConfig.enabled()) {
            Logger.getLogger(VersionRecorder.class.getName()).infof("Version: %s", version);
        }
    }

}
