package io.quarkus.workshop.superheroes.statistics;

public class Score {
    protected final String name;
    protected final int score;

    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "Score{" +
            "name='" + name + '\'' +
            ", score=" + score +
            '}';
    }
}
