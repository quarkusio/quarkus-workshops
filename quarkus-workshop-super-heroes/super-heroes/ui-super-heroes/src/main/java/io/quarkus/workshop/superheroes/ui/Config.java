package io.quarkus.workshop.superheroes.ui;

import io.quarkus.runtime.annotations.RegisterForReflection;

/*
Why do we need to register this for reflection? Normally this would be automatic if
we return it from a REST endpoint, but because we're handling our own
object mapping, we need to do our own registering.
 */
@RegisterForReflection
public record Config(String API_BASE_URL, boolean CALCULATE_API_BASE_URL) {
}
