// tag::adocBean[]
package io.quarkus.workshop.superheroes.statistics;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Score {
    public final String name;
    public final int score;

    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }
}
// end::adocBean[]
