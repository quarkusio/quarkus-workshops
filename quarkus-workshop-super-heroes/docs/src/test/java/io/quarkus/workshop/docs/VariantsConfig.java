package io.quarkus.workshop.docs;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VariantsConfig {

    private static final String CONFIG_PATH = System.getProperty("variants.config.path",
            "src/resource-generation/variants-config.json");

    private static VariantsConfig instance;

    private final List<Flag> flags;
    private final List<String> osOptions;

    public record Flag(String id, String label, boolean enabled, boolean defaultValue,
                       List<String> requires, boolean standalone) {
    }

    private VariantsConfig(List<Flag> flags, List<String> osOptions) {
        this.flags = flags;
        this.osOptions = osOptions;
    }

    public static synchronized VariantsConfig load() {
        if (instance == null) {
            try (JsonReader reader = Json.createReader(new FileReader(CONFIG_PATH))) {
                JsonObject config = reader.readObject();
                List<Flag> flags = parseFlags(config.getJsonArray("flags"));
                List<String> osOptions = parseStringArray(config.getJsonArray("osOptions"));
                instance = new VariantsConfig(flags, osOptions);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read variants config from " + CONFIG_PATH, e);
            }
        }
        return instance;
    }

    public List<Flag> flags() {
        return flags;
    }

    public List<String> osOptions() {
        return osOptions;
    }

    public List<Flag> enabledFlags() {
        return flags.stream().filter(Flag::enabled).toList();
    }

    public List<Flag> enabledNonStandaloneFlags() {
        return flags.stream().filter(f -> f.enabled() && !f.standalone()).toList();
    }

    public List<Flag> standaloneFlags() {
        return flags.stream().filter(f -> f.enabled() && f.standalone()).toList();
    }

    /**
     * Generates all valid combinations respecting constraints and standalone rules.
     * Returns a list of maps from flag id to boolean value.
     */
    public List<Map<String, Boolean>> generateValidCombinations(String os) {
        List<Map<String, Boolean>> combinations = new ArrayList<>();

        List<Flag> nonStandalone = enabledNonStandaloneFlags();
        List<Flag> standalone = standaloneFlags();

        // Main matrix: all combos of non-standalone flags, standalone flags all false
        int n = nonStandalone.size();
        for (int bits = 0; bits < (1 << n); bits++) {
            Map<String, Boolean> assignment = new LinkedHashMap<>();
            for (Flag f : flags) {
                if (!f.enabled()) {
                    assignment.put(f.id(), f.defaultValue());
                }
            }
            for (int i = 0; i < n; i++) {
                assignment.put(nonStandalone.get(i).id(), (bits & (1 << i)) != 0);
            }
            for (Flag sf : standalone) {
                assignment.put(sf.id(), false);
            }
            if (isValid(assignment)) {
                combinations.add(assignment);
            }
        }

        // Standalone variants: each standalone flag alone
        for (Flag sf : standalone) {
            Map<String, Boolean> assignment = new LinkedHashMap<>();
            for (Flag f : flags) {
                if (!f.enabled()) {
                    assignment.put(f.id(), f.defaultValue());
                } else {
                    assignment.put(f.id(), f.id().equals(sf.id()));
                }
            }
            combinations.add(assignment);
        }

        // Everything variant: all enabled flags true
        Map<String, Boolean> everything = new LinkedHashMap<>();
        for (Flag f : flags) {
            everything.put(f.id(), f.enabled() || f.defaultValue());
        }
        if (isValid(everything)) {
            combinations.add(everything);
        }

        return combinations;
    }

    public boolean isValid(Map<String, Boolean> assignment) {
        for (Flag f : flags) {
            if (Boolean.TRUE.equals(assignment.get(f.id()))) {
                for (String req : f.requires()) {
                    if (!Boolean.TRUE.equals(assignment.get(req))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static List<Flag> parseFlags(JsonArray arr) {
        List<Flag> flags = new ArrayList<>();
        for (JsonObject obj : arr.getValuesAs(JsonObject.class)) {
            flags.add(new Flag(
                    obj.getString("id"),
                    obj.getString("label"),
                    obj.getBoolean("enabled"),
                    obj.getBoolean("defaultValue"),
                    parseStringArray(obj.getJsonArray("requires")),
                    obj.getBoolean("standalone")
            ));
        }
        return flags;
    }

    private static List<String> parseStringArray(JsonArray arr) {
        List<String> result = new ArrayList<>();
        for (JsonString s : arr.getValuesAs(JsonString.class)) {
            result.add(s.getString());
        }
        return result;
    }
}
