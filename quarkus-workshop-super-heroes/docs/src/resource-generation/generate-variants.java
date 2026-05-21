///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS jakarta.json:jakarta.json-api:2.1.3
//DEPS org.eclipse.parsson:parsson:1.1.6

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class generate_variants {

    record Flag(String id, String label, boolean enabled, boolean defaultValue,
                List<String> requires, boolean standalone) {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: generate-variants.java <config.json> <output-dir> [os] [--template <template-path> <output-path>] [--defaults <output-path>]");
            System.exit(1);
        }

        String configPath = args[0];
        String outputDir = args[1];
        String osFilter = null;
        String templatePath = null;
        String templateOutputPath = null;
        String defaultsOutputPath = null;

        for (int i = 2; i < args.length; i++) {
            if ("--template".equals(args[i]) && i + 2 < args.length) {
                templatePath = args[++i];
                templateOutputPath = args[++i];
            } else if ("--defaults".equals(args[i]) && i + 1 < args.length) {
                defaultsOutputPath = args[++i];
            } else if (osFilter == null) {
                osFilter = args[i];
            }
        }

        JsonObject config;
        try (JsonReader reader = Json.createReader(new FileReader(configPath))) {
            config = reader.readObject();
        }

        List<Flag> flags = parseFlags(config.getJsonArray("flags"));
        List<String> osOptions = parseStringArray(config.getJsonArray("osOptions"));

        if (osFilter != null) {
            osOptions = List.of(osFilter);
        }

        List<Flag> enabledNonStandalone = flags.stream()
                .filter(f -> f.enabled() && !f.standalone())
                .toList();
        List<Flag> standaloneFlags = flags.stream()
                .filter(f -> f.enabled() && f.standalone())
                .toList();

        int variantCount = 0;

        for (String os : osOptions) {
            // Generate all valid combinations of non-standalone flags
            int nonStandaloneCount = enabledNonStandalone.size();
            for (int bits = 0; bits < (1 << nonStandaloneCount); bits++) {
                Map<String, Boolean> assignment = buildAssignment(flags, enabledNonStandalone, bits);
                // Standalone flags are false in the main matrix
                for (Flag sf : standaloneFlags) {
                    assignment.put(sf.id(), false);
                }

                if (!isValid(assignment, flags)) {
                    continue;
                }

                writeVariant(outputDir, os, flags, assignment);
                variantCount++;
            }

            // Generate standalone variants: each standalone flag alone (all others off)
            for (Flag sf : standaloneFlags) {
                Map<String, Boolean> assignment = new LinkedHashMap<>();
                for (Flag f : flags) {
                    if (!f.enabled()) {
                        assignment.put(f.id(), f.defaultValue());
                    } else {
                        assignment.put(f.id(), f.id().equals(sf.id()));
                    }
                }
                writeVariant(outputDir, os, flags, assignment);
                variantCount++;
            }

            // Generate the "everything" variant (all enabled flags true, if valid)
            Map<String, Boolean> everything = new LinkedHashMap<>();
            for (Flag f : flags) {
                everything.put(f.id(), f.enabled() || f.defaultValue());
            }
            if (isValid(everything, flags)) {
                writeVariant(outputDir, os, flags, everything);
                variantCount++;
            }
        }

        System.out.println("Generated " + variantCount + " variant directories");

        if (defaultsOutputPath != null) {
            writeDefaults(defaultsOutputPath, flags);
        }

        if (templatePath != null && templateOutputPath != null) {
            processTemplate(templatePath, templateOutputPath, config);
        }
    }

    private static Map<String, Boolean> buildAssignment(List<Flag> allFlags,
                                                         List<Flag> enabledNonStandalone, int bits) {
        Map<String, Boolean> assignment = new LinkedHashMap<>();
        for (Flag f : allFlags) {
            if (!f.enabled()) {
                assignment.put(f.id(), f.defaultValue());
            }
        }
        for (int i = 0; i < enabledNonStandalone.size(); i++) {
            assignment.put(enabledNonStandalone.get(i).id(), (bits & (1 << i)) != 0);
        }
        return assignment;
    }

    private static boolean isValid(Map<String, Boolean> assignment, List<Flag> flags) {
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

    private static void writeVariant(String outputDir, String os,
                                      List<Flag> flags, Map<String, Boolean> assignment) throws IOException {
        StringBuilder dirname = new StringBuilder("os-").append(os);
        for (Flag f : flags) {
            dirname.append("-").append(f.id()).append("-").append(assignment.get(f.id()));
        }

        Path dir = Path.of(outputDir, dirname.toString());
        Files.createDirectories(dir);

        Path optionsFile = dir.resolve("options.adoc");
        try (PrintWriter pw = new PrintWriter(optionsFile.toFile())) {
            pw.println(":os: " + os);
            for (Flag f : flags) {
                if (Boolean.TRUE.equals(assignment.get(f.id()))) {
                    pw.println(":use-" + f.id() + ":");
                }
            }
        }
        System.out.println("Created " + optionsFile);
    }

    private static void writeDefaults(String outputPath, List<Flag> flags) throws IOException {
        Path path = Path.of(outputPath);
        Files.createDirectories(path.getParent());
        try (PrintWriter pw = new PrintWriter(path.toFile())) {
            pw.println(":os: all");
            for (Flag f : flags) {
                if (f.defaultValue()) {
                    pw.println(":use-" + f.id() + ":");
                }
            }
        }
        System.out.println("Generated " + outputPath);
    }

    private static void processTemplate(String templatePath, String outputPath,
                                          JsonObject config) throws IOException {
        String template = Files.readString(Path.of(templatePath));
        String configJson = config.toString();
        String result = template.replace("__VARIANTS_CONFIG_JSON__", configJson);

        Path out = Path.of(outputPath);
        Files.createDirectories(out.getParent());
        Files.writeString(out, result);
        System.out.println("Processed template to " + outputPath);
    }

    private static List<Flag> parseFlags(JsonArray arr) {
        List<Flag> flags = new ArrayList<>();
        for (JsonObject obj : arr.getValuesAs(JsonObject.class)) {
            List<String> requires = parseStringArray(obj.getJsonArray("requires"));
            flags.add(new Flag(
                    obj.getString("id"),
                    obj.getString("label"),
                    obj.getBoolean("enabled"),
                    obj.getBoolean("defaultValue"),
                    requires,
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
