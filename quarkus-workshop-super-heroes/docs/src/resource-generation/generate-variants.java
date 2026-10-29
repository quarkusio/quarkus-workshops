/// usr/bin/env jbang "$0" "$@" ; exit $?

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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class generate_variants {

    record Flag(String id, String label, boolean enabled, boolean defaultValue,
                List<String> requires, boolean standalone) {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: generate-variants.java <config.json> <output-dir> [os] [--defaults <output-path>] [--copy-images <src-dir> <dest-dir>] [--spine-source <spine.adoc-path>] [--diagram-config-dir <dir>]");
            System.exit(1);
        }

        String configPath = args[0];
        String outputDir = args[1];
        String osFilter = null;
        String buildToolFilter = null;
        String defaultsOutputPath = null;
        String imagesSrcDir = null;
        String imagesDestDir = null;
        String spineSourcePath = null;
        String diagramConfigDir = null;

        for (int i = 2; i < args.length; i++) {
            if ("--defaults".equals(args[i]) && i + 1 < args.length) {
                defaultsOutputPath = args[++i];
            } else if ("--copy-images".equals(args[i]) && i + 2 < args.length) {
                imagesSrcDir = args[++i];
                imagesDestDir = args[++i];
            } else if ("--spine-source".equals(args[i]) && i + 1 < args.length) {
                spineSourcePath = args[++i];
            } else if ("--diagram-config-dir".equals(args[i]) && i + 1 < args.length) {
                diagramConfigDir = args[++i];
            } else if ("--buildTool".equals(args[i]) && i + 1 < args.length) {
                buildToolFilter = args[++i];
            } else if ("--os".equals(args[i]) && i + 1 < args.length) {
                osFilter = args[++i];
            } else if (osFilter==null) {
                osFilter = args[i];
            }
        }

        JsonObject config;
        try (JsonReader reader = Json.createReader(new FileReader(configPath))) {
            config = reader.readObject();
        }

        List<Flag> flags = parseFlags(config.getJsonArray("flags"));
        List<String> osOptions = parseStringArray(config.getJsonArray("osOptions"));
        List<String> buildToolOptions = config.containsKey("buildToolOptions")
            ? parseStringArray(config.getJsonArray("buildToolOptions"))
            :List.of("maven");

        if (osFilter!=null) {
            osOptions = List.of(osFilter);
        }
        if (buildToolFilter!=null) {
            buildToolOptions = List.of(buildToolFilter);
        }

        List<Flag> enabledNonStandalone = flags.stream()
            .filter(f -> f.enabled() && !f.standalone())
            .toList();
        List<Flag> standaloneFlags = flags.stream()
            .filter(f -> f.enabled() && f.standalone())
            .toList();

        Path spineSource = spineSourcePath != null ? Path.of(spineSourcePath) : null;

        Set<String> diagramKeys = new LinkedHashSet<>();
        int variantCount = 0;

        for (String buildTool : buildToolOptions) {
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

                    diagramKeys.add(diagramKey(assignment));
                    writeVariant(outputDir, buildTool, os, flags, assignment, spineSource, diagramConfigDir);
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
                    diagramKeys.add(diagramKey(assignment));
                    writeVariant(outputDir, buildTool, os, flags, assignment, spineSource, diagramConfigDir);
                    variantCount++;
                }

                // Generate the "everything" variant (all enabled flags true, if valid)
                Map<String, Boolean> everything = new LinkedHashMap<>();
                for (Flag f : flags) {
                    everything.put(f.id(), f.enabled() || f.defaultValue());
                }
                if (isValid(everything, flags)) {
                    diagramKeys.add(diagramKey(everything));
                    writeVariant(outputDir, buildTool, os, flags, everything, spineSource, diagramConfigDir);
                    variantCount++;
                }
            }
        }

        System.out.println("Generated " + variantCount + " variant directories");

        if (defaultsOutputPath!=null) {
            writeDefaults(defaultsOutputPath, flags, diagramConfigDir);
        }

        if (imagesSrcDir!=null && imagesDestDir!=null) {
            copyStaticImages(imagesSrcDir, imagesDestDir, diagramKeys);
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
            assignment.put(enabledNonStandalone.get(i).id(), (bits & (1 << i))!=0);
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

    private static void writeVariant(String outputDir, String buildTool, String os,
                                     List<Flag> flags, Map<String, Boolean> assignment,
                                     Path spineSource, String diagramConfigDir) throws IOException {
        StringBuilder dirname = new StringBuilder("bt-").append(buildTool)
            .append("-os-").append(os);
        for (Flag f : flags) {
            dirname.append("-").append(f.id()).append("-").append(assignment.get(f.id()));
        }

        Path dir = Path.of(outputDir, dirname.toString());
        Files.createDirectories(dir);

        Path optionsFile = dir.resolve("options.adoc");
        try (PrintWriter pw = new PrintWriter(optionsFile.toFile())) {
            pw.println(":buildtool: " + buildTool);
            pw.println(":os: " + os);
            for (Flag f : flags) {
                if (Boolean.TRUE.equals(assignment.get(f.id()))) {
                    pw.println(":use-" + f.id() + ":");
                }
            }
            writeDiagramAttributes(pw, assignment, diagramConfigDir);
        }
        System.out.println("Created " + optionsFile);

        // Copy spine.adoc into the variant directory if source is provided
        if (spineSource != null && Files.exists(spineSource)) {
            Path spineDestination = dir.resolve("spine.adoc");
            Files.copy(spineSource, spineDestination, StandardCopyOption.REPLACE_EXISTING);
        }

        writeDiagramConfig(diagramConfigDir, assignment);
    }

    private static void writeDefaults(String outputPath, List<Flag> flags, String diagramConfigDir) throws IOException {
        Path path = Path.of(outputPath);
        Files.createDirectories(path.getParent());

        Map<String, Boolean> assignment = new LinkedHashMap<>();
        for (Flag f : flags) {
            assignment.put(f.id(), f.defaultValue());
        }

        try (PrintWriter pw = new PrintWriter(path.toFile())) {
            pw.println(":buildtool: maven");
            pw.println(":os: all");
            for (Flag f : flags) {
                if (f.defaultValue()) {
                    pw.println(":use-" + f.id() + ":");
                }
            }
            writeDiagramAttributes(pw, assignment, diagramConfigDir);
        }
        System.out.println("Generated " + outputPath);

        writeDiagramConfig(diagramConfigDir, assignment);
    }

    // Only flags that appear in !ifdef guards in .puml files need distinct diagram renders.
    // Currently: ai, azure, messaging and observability. Update if new !ifdef guards are added to .puml files;
    // VariantDiagramsIT fails if a variant's diagrams do not match its flags.
    private static List<String> diagramFlags(Map<String, Boolean> assignment) {
        boolean ai = Boolean.TRUE.equals(assignment.get("ai"));
        boolean azure = Boolean.TRUE.equals(assignment.get("azure"));
        List<String> ids = new ArrayList<>();
        if (ai) ids.add("ai");
        // azure only changes a diagram in combination with ai
        if (ai && azure) ids.add("azure");
        if (Boolean.TRUE.equals(assignment.get("messaging"))) ids.add("messaging");
        if (Boolean.TRUE.equals(assignment.get("observability"))) ids.add("observability");
        return ids;
    }

    private static String diagramKey(Map<String, Boolean> assignment) {
        List<String> parts = new ArrayList<>();
        for (String id : diagramFlags(assignment)) {
            parts.add(switch (id) {
                case "messaging" -> "msg";
                case "observability" -> "obs";
                default -> id;
            });
        }
        return parts.isEmpty() ? "base":String.join("-", parts);
    }

    // Every diagram whose .puml has !ifdef guards names its output after {diagram-key}, so that
    // variants needing different renders of it write different files: they are all generated in one
    // Roq run into one images directory, and a shared file name would leave every variant showing
    // whichever render came last. The defines reach PlantUML through plantuml-config, which
    // asciidoctor-diagram reads as a document attribute. {diagram-config-dir} is set in
    // config/application.properties, and has to match the --diagram-config-dir argument.
    private static void writeDiagramAttributes(PrintWriter pw, Map<String, Boolean> assignment,
                                               String diagramConfigDir) {
        if (diagramConfigDir==null) {
            return;
        }
        String key = diagramKey(assignment);
        pw.println(":diagram-key: " + key);
        pw.println(":plantuml-config: {diagram-config-dir}/" + key + ".puml");
    }

    private static void copyStaticImages(String srcDir, String destDir,
                                         Set<String> diagramKeys) throws IOException {
        Path src = Path.of(srcDir);
        if (!Files.isDirectory(src)) {
            System.out.println("Images source directory not found: " + srcDir + " (skipping copy)");
            return;
        }

        for (String key : diagramKeys) {
            Path dest = Path.of(destDir, "d-" + key);
            Files.createDirectories(dest);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(src, "*.{png,jpg,svg}")) {
                for (Path file : stream) {
                    Path target = dest.resolve(file.getFileName());
                    if (!Files.exists(target)) {
                        Files.copy(file, target);
                    }
                }
            }
        }
        System.out.println("Copied static images to " + diagramKeys.size() + " diagram-key directories");
    }

    private static void writeDiagramConfig(String diagramConfigDir, Map<String, Boolean> assignment)
        throws IOException {
        if (diagramConfigDir==null) {
            return;
        }
        Path dir = Path.of(diagramConfigDir);
        Files.createDirectories(dir);
        Path configFile = dir.resolve(diagramKey(assignment) + ".puml");
        try (PrintWriter pw = new PrintWriter(configFile.toFile())) {
            for (String id : diagramFlags(assignment)) {
                pw.println("!define use_" + id);
            }
        }
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
