package io.quarkus.workshop.superheroes.version.runtime;

import io.quarkus.runtime.annotations.Recorder;
import org.jboss.logging.Logger;

@Recorder
public class VersionRecorder {

    public void printVersion(String version) {
        Logger.getLogger(VersionRecorder.class.getName()).infof("Version: %s", version);
    }

}
