///usr/bin/env jbang "$0" "$@" ; exit $?
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import static java.lang.System.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class checkports {

    static boolean isPortFree(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
        return false;
    } catch (ConnectException e) {
        return true;
    } catch (IOException e) {
        throw new IllegalStateException("Error while trying to check open port", e);
    }
    }
    public static void main(String... args) {
        Map<Integer, String> ports = new HashMap<>();
        ports.put(8080   , "UI");
        ports.put(8082   , "Fight REST API");
        ports.put(8083   , "Hero REST API");
        ports.put(8084   , "Villain REST API");
        ports.put(8085   , "Statistics REST API");
        ports.put(8086   , "Narration REST API");
        ports.put(5432   , "Postgres");
        ports.put(9090   , "Prometheus");
        ports.put(2181   , "Zookeeper");
        ports.put(9092   , "Kafka");

        ports.entrySet().removeIf(entry -> isPortFree(entry.getKey()));
        
        if(ports.isEmpty()) {
            out.println("All ports free. You can run the workshop!");
        } else {
            out.println("Unavailable ports:");
            ports.forEach((port, name) -> {
                out.println("Port " + port + " (" + name + ")");
            });

            out.println("Please free these ports before running the workshop.");
        }
    }
}
