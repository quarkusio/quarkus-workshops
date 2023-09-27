# Hosts Quarkus related workshops

* quarkus-workshop-super-heroes: workshop where you build several microservices interoperating through HTTP and Kafka.
  Instructions are available [here](https://quarkus.io/quarkus-workshops/super-heroes/).

## Configuring a workshop

The [entry page of the workshop](https://quarkus.io/quarkus-workshops/super-heroes/) accepts query parameters.

For
example, https://quarkus.io/quarkus-workshops/super-heroes/index.html?native=false&ai=false&kubernetes=false&contract-testing=false&observability=false&extension=false&messaging=false&hideDefined=true
will
present a minimal workshop with no optional components, but allow people to select an operating system.
The `hideDefined=true` locks and hides options which have been set in the url.

As another example, https://quarkus.io/quarkus-workshops/super-heroes/index.html?os=mac would preconfigure the operating
system to MacOS,
and show all options.

## Continuous Build

Each push and pull requests is checked using GitHub Actions.