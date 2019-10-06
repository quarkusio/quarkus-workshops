package io.quarkus.workshop.superheroes.statistics;

import io.reactivex.Maybe;

import java.util.Collections;
import java.util.TreeSet;

public class Ranking {

    private final int max;

    private final TreeSet<Score> top = new TreeSet<>((s1, s2) -> {
        // Same name, so equal
        if (s1.name.equals(s2.name)) {
            return 0;
        }

        if (s2.score > s1.score) {
            return 1;
        } else if (s2.score < s1.score) {
            return -1;
        }
        // Pick the new one.
        return 1;
    });

    public Ranking(int size) {
        max = size;
    }

    public synchronized Maybe<Iterable<Score>> onNewScore(Score score) {
        // synchronized should not be required if used in a flatMap, as the call needs to be serialized.

        top.remove(score);
        top.add(score);

        if (top.size() > max) {
            top.remove(top.last());
        }

        if (top.contains(score)) {
            return Maybe.just(Collections.unmodifiableSet(top));
        } else {
            return Maybe.empty();
        }
    }
}
