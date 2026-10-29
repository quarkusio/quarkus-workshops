package io.quarkus.workshop.superheroes.version.runtime;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import org.jboss.logging.Logger;

@Recorder
public class VersionRecorder {

    private final RuntimeValue<VersionConfig> versionConfig;

    public VersionRecorder(RuntimeValue<VersionConfig> versionConfig) {
        this.versionConfig = versionConfig;
    }

    public void printVersion(String version) {
        if (versionConfig.getValue().enabled()) {
            Logger.getLogger(VersionRecorder.class.getName()).infof("Version: %s", version);
        }
    }

}
