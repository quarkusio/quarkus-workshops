package io.quarkus.workshop.superheroes.statistics;

// tag::adocJavadoc[]
/**
 * Object keeping track of the number of battles won by heroes and villains
 */
// end::adocJavadoc[]
class TeamStats {

    private int villains = 0;
    private int heroes = 0;

    // tag::adocJavadoc[]
    /**
     * Adds a {@link Fight}
     * @param result The {@link Fight} received
     * @return A double containing running battle stats by team
     */
    // end::adocJavadoc[]
    double add(Fight result) {
        if (result.winnerTeam.equalsIgnoreCase("heroes")) {
            heroes = heroes + 1;
        } else {
            villains = villains + 1;
        }
        return ((double) heroes / (heroes + villains));
    }
}
