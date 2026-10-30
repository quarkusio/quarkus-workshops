package io.quarkus.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

/**
 * Gives diagrams and images a dark-mode copy where they have one.
 * The work is done in asciidoctor/dark-mode.rb; this class only gets it loaded.
 * <p>
 * Roq creates its Asciidoctor instance itself and offers no hook for extensions, but
 * {@code Asciidoctor.Factory.create()} picks up any {@link ExtensionRegistry} listed in
 * META-INF/services, so that is how this gets in.
 */
public class DarkModeExtensionRegistry implements ExtensionRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {
        // The Ruby side patches asciidoctor-diagram's processor classes, so they have to be loaded
        // first. Roq requires the library again straight after this, which is then a no-op.
        asciidoctor.requireLibrary("asciidoctor-diagram");
        try (InputStream script = getClass().getResourceAsStream("/asciidoctor/dark-mode.rb")) {
            asciidoctor.rubyExtensionRegistry().loadClass(script);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
