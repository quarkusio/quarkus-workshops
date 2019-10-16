package io.quarkus.workshop.superheroes.banner.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BannerRecorder {

    public void configureBannerBean(BeanContainer container, String path) {
        container.instance(BannerBean.class).setup(path);
    }
}
