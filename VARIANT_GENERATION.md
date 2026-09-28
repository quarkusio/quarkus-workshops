# Variant Generation Architecture

This document describes how the workshop generates and serves 664 variant combinations of the documentation.

## Overview

The system generates all valid combinations of:
- **Build tools**: Maven, Gradle (2 options)
- **Operating systems**: All, Linux, Mac, Windows (4 options)  
- **Feature flags**: AI, Azure, Container, Kubernetes, Messaging, Native, etc. (configurable per flag)

Total: ~664 combinations based on flag constraints and standalone requirements.

## Components

### 1. Variant Source of Truth: `data/variants.json`

Central configuration file defining:
- All feature flags (id, label, enabled, defaultValue, requires, standalone)
- Build tool options
- OS options
- Constraints between flags

This file is also exposed as a CDI bean via Roq's `roq-data` plugin and injected into the configurator template with `{=cdi:variants}`.

### 2. Variant Generation: `generate-variants.java`

JBang script that reads `data/variants.json` and generates 664 directories, one per combination.

**Phase**: `generate-resources` (runs early, before Roq build)

**Output**: `target/variant-combinations/bt-{buildTool}-os-{os}-{flag1}-{value1}-{flag2}-{value2}-...`

Each directory contains:
- `options.adoc`: AsciiDoc variable definitions for that combination
- `plantuml-config.puml`: PlantUML macro definitions for diagrams

### 3. Variant Configuration CDI Bean: `VariantConfig.java`

CDI producer (`@ApplicationScoped`) that:
- Reads `workshop.variant.name` property
- Parses it to extract build tool, OS, and flag values
- Falls back to defaults from `data/variants.json` if no variant specified
- Produces a `JsonObject` bean with variant flags

Templates access it as: `{=cdi:variantConfig.getString('os')}`

### 4. Multi-Variant Build: `iterator-maven-plugin`

**Status**: Prepared in pom.xml but not yet active (requires additional setup)

When enabled, will:
1. Iterate over each directory in `target/variant-combinations/`
2. Set `workshop.variant.name` to the directory name
3. Run `quarkus:build` for that variant
4. Generate variant-specific HTML with flags from `VariantConfig`

Configuration in pom.xml:
```xml
<plugin>
  <groupId>com.soebes.maven.plugins</groupId>
  <artifactId>iterator-maven-plugin</artifactId>
  <version>0.5.1</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals><goal>iterator</goal></goals>
      <configuration>
        <folder>${basedir}/target/variant-combinations</folder>
        <pluginExecutors>
          <pluginExecutor>
            <plugin>
              <groupId>io.quarkus</groupId>
              <artifactId>quarkus-maven-plugin</artifactId>
            </plugin>
            <goal>build</goal>
            <configuration>
              <systemPropertyVariables>
                <workshop.variant.name>@item.name@</workshop.variant.name>
              </systemPropertyVariables>
            </configuration>
          </pluginExecutor>
        </pluginExecutors>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Current State

✅ **Done**:
- Variants are generated during `generate-resources`
- 664 combination directories exist in `target/variant-combinations/`
- `VariantConfig` CDI bean is ready and tested
- Templates can access variant config via `cdi:variantConfig`

⏳ **Ready but not yet active**:
- iterator-maven-plugin configuration is in pom.xml
- To enable multi-variant builds, uncomment the plugin and ensure Roq runs in a way that respects the `workshop.variant.name` property

## Usage

### Local Development
```bash
# Build single site with default variants
mvn clean package quarkus:run -DskipTests -Dos=linux

# Tests still work normally
mvn test
```

### Building Specific Variant (when iterator is enabled)
```bash
# Build a single variant manually
mvn clean package -Dworkshop.variant.name=bt-maven-os-linux-ai-true-container-false-...
```

### Full Multi-Variant Build (when iterator is enabled)
```bash
# Will build all 664 variants (slow)
mvn clean package
```

## Design Decisions

1. **Early Generation**: Variants are generated in `generate-resources`, before the Roq build, so they're available as a resource.

2. **CDI Injection**: Using `@Produces` and `{=cdi:variantConfig}` instead of build-time placeholder substitution keeps templates clean and enables runtime variant selection.

3. **Single Source of Truth**: `data/variants.json` is the only place flag definitions and constraints live.

4. **Roq Native**: Uses Roq's built-in `roq-data` for storing and injecting the variants JSON, fitting naturally into the Roq ecosystem.

## Next Steps

To enable full multi-variant generation:
1. Uncomment the iterator-maven-plugin in pom.xml
2. Ensure Roq respects variant-specific settings from `VariantConfig`
3. Update publication workflow to aggregate HTML from all variants
4. Consider caching variant builds to speed up CI
