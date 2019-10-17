package io.quarkus.workshop.superheroes.banner.runtime;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BannerRecorder {

    public void print(String banner) {
        System.err.println(banner);
    }
}
