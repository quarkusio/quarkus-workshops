package io.quarkus.workshop.superheroes.banner.runtime;

import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Scanner;

@ApplicationScoped
public class BannerBean {

    private String path;

    public void setup(String path) {
        this.path = path;
    }

    void onStart(@Observes StartupEvent ev) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("Unable to find the banner: " + path);
        }
        print(resource);
    }

    private void print(URL resource) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(resource.openStream()))) {
            Scanner scanner = new Scanner(in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
