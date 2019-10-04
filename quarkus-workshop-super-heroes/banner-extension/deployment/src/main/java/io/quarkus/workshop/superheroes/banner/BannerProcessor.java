package io.quarkus.workshop.superheroes.banner;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.substrate.SubstrateResourceBuildItem;
import io.quarkus.workshop.superheroes.banner.runtime.BannerBean;
import io.quarkus.workshop.superheroes.banner.runtime.BannerConfig;
import io.quarkus.workshop.superheroes.banner.runtime.BannerRecorder;

public class BannerProcessor {

    @BuildStep
    AdditionalBeanBuildItem registerBannerBean() {
        return AdditionalBeanBuildItem.unremovableOf(BannerBean.class);
    }

    @BuildStep
    void addResourceToNativeImage(BuildProducer<SubstrateResourceBuildItem> resource, BannerConfig config) {
        resource.produce(new SubstrateResourceBuildItem(config.file));
    }

    @BuildStep
    FeatureBuildItem registerExtensionName() {
        return new FeatureBuildItem("banner");
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    public void configureBannerBean(BeanContainerBuildItem beanContainer, BannerRecorder recorder, BannerConfig config) {
        recorder.init(beanContainer.getValue(), config);
    }
}
