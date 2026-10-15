package io.quarkus.tools;

import io.quarkiverse.roq.plugin.asciidoctorj.runtime.AsciidoctorJConfig;
import io.quarkus.qute.TemplateGlobal;
import io.smallrye.config.SmallRyeConfig;
import jakarta.inject.Inject;

//@TemplateData(namespace = "attributes")
//@ApplicationScoped
@TemplateGlobal(name = "attributes")
public class Globals {

    private static SmallRyeConfig config;

    @Inject
    AsciidoctorJConfig attributes;

    // Rather tediously, every asciidoc attribute we care about needs to be in this file
    static String assetsdir() {
        return readFromConfig("assetsdir");
    }

    static String imagesdir() {
        return readFromConfig("imagesdir");
    }

    private static String readFromConfig(String name) {
        SmallRyeConfig config = getConfig();
        String value = config.getValue("quarkus.asciidoc.attributes." + name, String.class);
        return value;
    }

    private static SmallRyeConfig getConfig() {
        if (config==null) {
            config = org.eclipse.microprofile.config.ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        }
        return config;

    }

}

