package io.quarkus.workshop.docs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that every diagram with {@code !ifdef} guards is rendered to a file named for the
 * variant's flags.
 * <p>
 * Without {@code target=<name>-{diagram-key}}, asciidoctor-diagram names the render after the
 * .puml file, and since all the variants are generated in one Roq run into one images directory,
 * they overwrite each other's renders. {@link VariantDiagramsIT} notices the result, but only
 * after a full build, and its failures do not say what the fix is. This reads the sources, so it
 * needs no build at all.
 */
public class DiagramTargetsTest {

    private static final Path CONTENT = Path.of("content");
    private static final Pattern MACRO = Pattern.compile("^plantuml::([^\\[]+\\.puml)\\[(.*)]\\s*$");

    @Test
    void guardedDiagramsShouldBeNamedForTheirVariant() throws IOException {
        List<String> problems = new ArrayList<>();
        int guarded = 0;
        List<Path> pages;
        try (Stream<Path> files = Files.walk(CONTENT)) {
            pages = files.filter(p -> p.getFileName().toString().endsWith(".adoc")).sorted().toList();
        }
        for (Path page : pages) {
            List<String> lines = Files.readAllLines(page);
            for (int i = 0; i < lines.size(); i++) {
                Matcher m = MACRO.matcher(lines.get(i));
                if (!m.matches()) {
                    continue;
                }
                String file = m.group(1).substring(m.group(1).lastIndexOf('/') + 1);
                Path puml = Diagrams.PLANTUML_DIR.resolve(file);
                if (!Files.exists(puml) || !hasGuards(puml)) {
                    continue;
                }
                guarded++;
                String target = "target=" + file.substring(0, file.length() - ".puml".length()) + "-{diagram-key}";
                if (!m.group(2).contains(target)) {
                    problems.add(page + ":" + (i + 1) + " renders " + file
                        + ", which has !ifdef guards; add " + target + " to its attributes");
                }
            }
        }
        assertTrue(guarded > 0, "Found no plantuml:: macros for diagrams with !ifdef guards under " + CONTENT
            + "; has the macro syntax changed?");
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    private static boolean hasGuards(Path puml) throws IOException {
        try (Stream<String> lines = Files.lines(puml)) {
            return lines.map(String::trim).anyMatch(l -> l.startsWith("!ifdef ") || l.startsWith("!ifndef "));
        }
    }
}
