package io.quarkus.workshop.docs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

/**
 * Checks that the diagrams on a generated page were rendered for the variant the page is for.
 * <p>
 * The .puml sources in {@code content/plantuml} use {@code !ifdef use_<flag>} guards to add parts
 * of the architecture (Kafka, the AI narration service, ...) only for the variants that cover
 * them. Nothing about a rendered page shows whether those guards saw the right flags: a diagram
 * rendered for the wrong variant is still a perfectly good image. But PlantUML embeds the source
 * it actually rendered, with the {@code !ifdef}s already resolved, in an iTXt chunk of every PNG
 * it writes. So for each diagram a page references, this works out which labels the guards should
 * have kept and which they should have dropped, and looks for them in that embedded source.
 */
final class Diagrams {

    /** Where the .puml sources live, relative to the docs module, which is where the tests run. */
    static final Path PLANTUML_DIR = Path.of("content", "plantuml");

    private static final Pattern IMG_SRC = Pattern.compile("<img\\b[^>]*\\bsrc=\"([^\"]+\\.png)\"");
    private static final Pattern QUOTED = Pattern.compile("\"([^\"]+)\"");
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};

    private Diagrams() {
    }

    /**
     * Problems with the diagrams on {@code page}, or an empty list if every one matches
     * {@code flags}. Fails, rather than passing vacuously, if the page has no diagram rendered
     * from a .puml file at all.
     *
     * @param siteRoot the root of the generated site, which {@code src="/..."} urls resolve against
     * @param sitePath the prefix the site is served under, which those urls carry, or empty
     * @param flags    the variant's flags, by id, as in data/variants.json
     */
    static List<String> problems(Path page, Path siteRoot, String sitePath, Map<String, Boolean> flags)
        throws IOException {
        Set<String> defined = flags.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(e -> "use_" + e.getKey())
            .collect(Collectors.toSet());
        List<Path> sources = pumlSources();

        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (String src : imageSources(page)) {
            String fileName = src.substring(src.lastIndexOf('/') + 1);
            Optional<Path> puml = sourceFor(fileName, sources);
            if (puml.isEmpty()) {
                // A hand-made image, or an inline [plantuml] block; neither has variant guards.
                continue;
            }
            checked++;
            Path png = resolve(src, page, siteRoot, sitePath);
            if (!Files.isRegularFile(png)) {
                problems.add(src + " is missing");
                continue;
            }
            String rendered = embeddedSource(png);
            if (rendered==null) {
                problems.add(src + " has no embedded PlantUML source");
                continue;
            }
            Labels labels = labels(puml.get(), defined);
            for (String label : labels.kept()) {
                if (!rendered.contains(quoted(label))) {
                    problems.add(src + " is missing \"" + label + "\" from " + puml.get().getFileName());
                }
            }
            for (String label : labels.dropped()) {
                if (rendered.contains(quoted(label))) {
                    problems.add(src + " should not have \"" + label + "\" from " + puml.get().getFileName());
                }
            }
        }
        if (checked==0) {
            problems.add(page + " has no diagrams rendered from " + PLANTUML_DIR);
        }
        return problems;
    }

    /** The labels a .puml's guards keep and drop when exactly {@code defined} are defined. */
    record Labels(Set<String> kept, Set<String> dropped) {
    }

    static Labels labels(Path puml, Set<String> defined) throws IOException {
        Set<String> kept = new LinkedHashSet<>();
        Set<String> dropped = new LinkedHashSet<>();
        // One entry per enclosing guard: whether that guard's current branch is taken.
        Deque<Boolean> guards = new ArrayDeque<>();
        for (String line : Files.readAllLines(puml)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("'")) {
                continue;
            } else if (trimmed.startsWith("!ifdef ")) {
                guards.push(defined.contains(trimmed.substring("!ifdef ".length()).trim()));
            } else if (trimmed.startsWith("!ifndef ")) {
                guards.push(!defined.contains(trimmed.substring("!ifndef ".length()).trim()));
            } else if (trimmed.equals("!else")) {
                guards.push(!guards.pop());
            } else if (trimmed.equals("!endif")) {
                guards.pop();
            } else if (trimmed.startsWith("!if") || trimmed.startsWith("!elseif")) {
                throw new IllegalStateException(puml + " uses " + trimmed
                    + ", which Diagrams.labels does not understand; teach it, or use !ifdef");
            } else {
                Set<String> into = guards.contains(false) ? dropped:kept;
                Matcher m = QUOTED.matcher(line);
                while (m.find()) {
                    into.add(m.group(1));
                }
            }
        }
        // A label used both inside and outside a dropped block is still on the diagram.
        dropped.removeAll(kept);
        return new Labels(kept, dropped);
    }

    /** The source PlantUML embedded in a PNG it rendered, or null if there is none. */
    static String embeddedSource(Path png) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(png)))) {
            if (!Arrays.equals(in.readNBytes(PNG_SIGNATURE.length), PNG_SIGNATURE)) {
                return null;
            }
            while (true) {
                int length;
                try {
                    length = in.readInt();
                } catch (EOFException e) {
                    return null;
                }
                String type = new String(in.readNBytes(4), StandardCharsets.US_ASCII);
                byte[] data = in.readNBytes(length);
                in.skipNBytes(4); // CRC
                if (type.equals("iTXt")) {
                    String text = iTXt(data, "plantuml");
                    if (text!=null) {
                        return text;
                    }
                } else if (type.equals("IEND")) {
                    return null;
                }
            }
        }
    }

    // iTXt is keyword \0 compression-flag compression-method language \0 translated-keyword \0 text.
    private static String iTXt(byte[] data, String wantedKeyword) throws IOException {
        int keywordEnd = indexOf(data, 0, 0);
        String keyword = new String(data, 0, keywordEnd, StandardCharsets.ISO_8859_1);
        if (!keyword.equals(wantedKeyword)) {
            return null;
        }
        boolean compressed = data[keywordEnd + 1]!=0;
        int languageEnd = indexOf(data, keywordEnd + 3, 0);
        int translatedEnd = indexOf(data, languageEnd + 1, 0);
        int textStart = translatedEnd + 1;
        InputStream raw = new ByteArrayInputStream(data, textStart, data.length - textStart);
        try (InputStream text = compressed ? new InflaterInputStream(raw):raw) {
            return new String(text.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static int indexOf(byte[] data, int from, int value) {
        for (int i = from; i < data.length; i++) {
            if (data[i]==value) {
                return i;
            }
        }
        throw new IllegalArgumentException("Malformed iTXt chunk");
    }

    private static List<Path> pumlSources() throws IOException {
        try (Stream<Path> files = Files.list(PLANTUML_DIR)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".puml")).toList();
        }
    }

    // An image rendered from foo.puml is named foo.png, or foo-<something>.png when the render is
    // specific to a variant. The longest match wins, so 5-x.puml cannot claim 5-x-y.puml's images.
    private static Optional<Path> sourceFor(String pngName, List<Path> sources) {
        String name = pngName.substring(0, pngName.length() - ".png".length());
        return sources.stream()
            .filter(p -> {
                String base = baseName(p);
                return name.equals(base) || name.startsWith(base + "-");
            })
            .max(Comparator.comparingInt(p -> baseName(p).length()));
    }

    private static String baseName(Path puml) {
        String name = puml.getFileName().toString();
        return name.substring(0, name.length() - ".puml".length());
    }

    private static List<String> imageSources(Path page) throws IOException {
        Matcher m = IMG_SRC.matcher(Files.readString(page));
        List<String> sources = new ArrayList<>();
        while (m.find()) {
            sources.add(m.group(1));
        }
        return sources.stream().distinct().toList();
    }

    private static Path resolve(String src, Path page, Path siteRoot, String sitePath) {
        if (!src.startsWith("/")) {
            return page.resolveSibling(src).normalize();
        }
        String path = !sitePath.isEmpty() && src.startsWith(sitePath + "/") ? src.substring(sitePath.length()):src;
        return siteRoot.resolve(path.substring(1)).normalize();
    }

    private static String quoted(String label) {
        return "\"" + label + "\"";
    }
}
