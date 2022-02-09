package io.quarkus.workshop.superheroes.version.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationInfoBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.workshop.superheroes.version.runtime.VersionRecorder;

class ExtensionVersionProcessor {

    private static final String FEATURE = "extension-version";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void recordVersion(ApplicationInfoBuildItem app, VersionConfig versionConfig, VersionRecorder recorder) {
        if (versionConfig.enabled) {
            recorder.printVersion(app.getVersion());
        }
    }
}
