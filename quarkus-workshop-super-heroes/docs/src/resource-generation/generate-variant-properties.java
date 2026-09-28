///usr/bin/env jbang "$0" "$@" ; exit $?

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generate application.properties file for each variant directory.
 * Each properties file sets workshop.variant.name to the directory name,
 * which is picked up by MicroProfile Config when Quarkus runs.
 */
class generate_variant_properties {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: generate-variant-properties.java <variant-combinations-dir>");
            System.exit(1);
        }

        Path variantDir = Path.of(args[0]);
        if (!Files.isDirectory(variantDir)) {
            System.err.println("Variant directory not found: " + variantDir);
            System.exit(1);
        }

        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(variantDir)) {
            for (Path variant : stream) {
                if (Files.isDirectory(variant)) {
                    String variantName = variant.getFileName().toString();
                    Path propsFile = variant.resolve("application.properties");
                    String content = "workshop.variant.name=" + variantName + "\n";
                    Files.writeString(propsFile, content);
                    count++;
                }
            }
        }

        System.out.println("Generated application.properties for " + count + " variants");
    }
}
