package io.quarkus.workshop.docs;

import java.io.FileReader;
import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * CDI producer for variant configuration.
 * Exposes the current variant's flags as a JsonObject bean accessible via {=cdi:variantConfig}
 * in Qute templates.
 *
 * When building a specific variant via iterator-maven-plugin, the variant name is passed via:
 *   -Dworkshop.variant.name=bt-maven-os-linux-ai-true-container-false-...
 *
 * Falls back to loading default flag values from data/variants.json if no variant specified.
 */
@ApplicationScoped
public class VariantConfig {
    private final JsonObject variantFlags;

    public VariantConfig(@ConfigProperty(name = "workshop.variant.name", defaultValue = "") String variantName) {
        this.variantFlags = loadVariantConfig(variantName);
    }

    @Produces
    public JsonObject getVariantFlags() {
        return variantFlags;
    }

    private static JsonObject loadVariantConfig(String variantName) {
        // If no variant specified, use defaults (all flags from variants.json defaultValue)
        if (variantName == null || variantName.isEmpty()) {
            return loadDefaultVariantConfig();
        }

        // Parse the variant name to extract flag settings
        // Format: bt-maven-os-linux-ai-true-container-false-...
        try {
            return parseVariantName(variantName);
        } catch (Exception e) {
            // Fall back to defaults
            return loadDefaultVariantConfig();
        }
    }

    private static JsonObject parseVariantName(String variantName) {
        // Extract build tool, OS, and flags from variant name
        // Expected format: bt-{buildTool}-os-{os}-flag1-{value1}-flag2-{value2}-...
        String[] parts = variantName.split("-");

        jakarta.json.JsonObjectBuilder builder = Json.createObjectBuilder();

        // Skip the "bt-" prefix
        int i = 1;

        // Get build tool (e.g., "maven")
        if (i < parts.length) {
            builder.add("buildTool", parts[i]);
            i++;
        }

        // Skip "os" keyword
        if (i < parts.length && "os".equals(parts[i])) {
            i++;
        }

        // Get OS (e.g., "linux")
        if (i < parts.length) {
            builder.add("os", parts[i]);
            i++;
        }

        // Parse remaining flag=value pairs
        while (i < parts.length - 1) {
            String flagName = parts[i];
            String flagValue = parts[i + 1];
            builder.add("use-" + flagName, "true".equals(flagValue));
            i += 2;
        }

        return builder.build();
    }

    private static JsonObject loadDefaultVariantConfig() {
        try (JsonReader reader = Json.createReader(new FileReader("data/variants.json"))) {
            JsonObject config = reader.readObject();
            JsonArray flags = config.getJsonArray("flags");

            jakarta.json.JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("buildTool", "maven");
            builder.add("os", "all");

            for (JsonObject flag : flags.getValuesAs(JsonObject.class)) {
                if (flag.getBoolean("enabled", false)) {
                    builder.add("use-" + flag.getString("id"), flag.getBoolean("defaultValue", false));
                }
            }

            return builder.build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load variant config", e);
        }
    }

    public JsonObject getFlags() {
        return variantFlags;
    }

    public String getBuildTool() {
        return variantFlags.getString("buildTool", "maven");
    }

    public String getOs() {
        return variantFlags.getString("os", "all");
    }

    public boolean isFlagEnabled(String flagId) {
        return variantFlags.getBoolean("use-" + flagId, false);
    }
}
