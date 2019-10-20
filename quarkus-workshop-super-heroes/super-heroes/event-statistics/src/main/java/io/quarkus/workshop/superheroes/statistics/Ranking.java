// tag::adocBean[]
package io.quarkus.workshop.superheroes.statistics;

import io.reactivex.Maybe;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Ranking {

    private final int max;

    private final Comparator<Score> comparator = (s1, s2) -> {
        if (s2.score > s1.score) {
            return 1;
        } else if (s2.score < s1.score) {
            return -1;
        }
        return 0;
    };

    private LinkedList<Score> top = new LinkedList<>();

    public Ranking(int size) {
        max = size;
    }

    public synchronized Maybe<Iterable<Score>> onNewScore(Score score) {
        // synchronized should not be required if used in a flatMap, as the call needs to be serialized.

        // Remove score if already present,
        // Add the score
        // Sort
        top.removeIf(s -> s.name.equalsIgnoreCase(score.name));
        top.add(score);
        top.sort(comparator);

        // Drop on overflow
        if (top.size() > max) {
            top.remove(top.getLast());
        }

        if (top.contains(score)) {
            return Maybe.just(Collections.unmodifiableList(top));
        } else {
            return Maybe.empty();
        }
    }
}
// end::adocBean[]
