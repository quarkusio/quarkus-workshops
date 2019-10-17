package io.quarkus.workshop.superheroes.banner.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;
import io.quarkus.deployment.util.FileUtil;
import io.quarkus.workshop.superheroes.banner.runtime.BannerRecorder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BannerProcessor {

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public void recordBanner(BannerRecorder recorder, BannerConfig config) {
        String content = readBannerFile(config.path);
        recorder.print(content);
    }

    @BuildStep
    List<HotDeploymentWatchedFileBuildItem> watchBannerChanges(BannerConfig config) {
        List<HotDeploymentWatchedFileBuildItem> watchedFiles = new ArrayList<>();
        watchedFiles.add(new HotDeploymentWatchedFileBuildItem((config.path)));
        return watchedFiles;
    }

    private String readBannerFile(String path) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource != null) {
            try (InputStream is = resource.openStream()) {
                byte[] content = FileUtil.readFileContents(is);
                return new String(content, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            throw new IllegalArgumentException("Cannot find the banner file: " + path);
        }
    }

}
