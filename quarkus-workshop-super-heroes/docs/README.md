## How to build the docs

You can run `quarkus dev` to build and serve the docs.
To build a non-default variant, uncomment `workshop.variant.name` in application.properties, and edit the value.

Run `mvn install` to generate a single set of instructions, with the default variant configuration.
Run `mvn -Dos=[linux|mac|windows|all] -Dfull` to generate a set of variants for a single OS. That should take around
2-3m.

