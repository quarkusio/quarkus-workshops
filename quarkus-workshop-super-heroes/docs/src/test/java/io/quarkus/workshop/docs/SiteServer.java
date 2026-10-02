package io.quarkus.workshop.docs;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import com.sun.net.httpserver.SimpleFileServer.OutputLevel;

/**
 * Serves the generated site over http for the duration of the test run.
 * <p>
 * The tests used to load pages over {@code file://}, which quietly lies about anything that
 * depends on a document root: a {@code src="/assets/..."} resolves to the filesystem root, and
 * nothing can tell a missing image from one the browser declined to fetch. Serving the directory
 * means the tests see what a reader of the published site sees.
 * <p>
 * This is the JDK's own {@link SimpleFileServer}, so there is nothing to install, no port to
 * coordinate with Maven, and it works the same from {@code mvn verify}, from {@code -Dtesty}, and
 * from an IDE. Pass {@code -Dtest.url} to test against a real server instead.
 */
final class SiteServer {

    private static String url;

    private SiteServer() {
    }

    /**
     * Starts the server on a free port the first time it is called, and returns its base url.
     */
    static synchronized String start(File root) {
        if (url == null) {
            Path directory = root.toPath().toAbsolutePath().normalize();
            if (!Files.isDirectory(directory)) {
                throw new IllegalStateException("No generated site to serve at " + directory
                    + ". Run `mvn verify` to generate it, or set -Ddocs.base.path to point at one.");
            }
            HttpServer server = SimpleFileServer.createFileServer(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), directory, OutputLevel.NONE);
            // The default is to handle requests one at a time on the dispatch thread, which
            // serialises every image fetch of every parallel test class.
            server.setExecutor(Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "site-server");
                t.setDaemon(true);
                return t;
            }));
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
            url = "http://localhost:" + server.getAddress().getPort();
        }
        return url;
    }
}
