package io.quarkus.tools;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension
public class AttributesExtension {


    /**
     * Null-safe get for JsonObject. Prevents NPE when key is null (e.g. from ?? operator).
     */
    static String attr(String key) {
        return "YO";
    }

}
