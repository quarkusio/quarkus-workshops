package io.quarkus.workshop.superheroes.statistics;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data class for a score
 * <p>
 *   The {@link RegisterForReflection @RegisterForReflection} annotation instructs the native compilation to allow reflection access to the class. Without it, the serialization/deserialization would not work when running the native executable.
 * </p>
 */
@RegisterForReflection
public class Score {
    public String name;
    public int score;

    public Score() {
        this.score = 0;
    }
}
