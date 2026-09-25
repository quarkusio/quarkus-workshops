#!/bin/bash
set -e

# Script to patch quarkus-roq AsciidoctorJConverter to support UNSAFE safe mode
# This is a temporary workaround until roq 2.1.10+ is released with configurable safe mode support

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
M2_REPO="${HOME}/.m2/repository"
ROQ_VERSION="${1:-2.1.9}"

ASCIIDOC_RUNTIME_JAR="${M2_REPO}/io/quarkiverse/roq/quarkus-roq-plugin-asciidoc-jruby/${ROQ_VERSION}/quarkus-roq-plugin-asciidoc-jruby-${ROQ_VERSION}.jar"
MARKER_FILE="${ASCIIDOC_RUNTIME_JAR}.patched"

# Check if already patched
if [ -f "${MARKER_FILE}" ]; then
    echo "Roq AsciidoctorJConverter already patched (marker file exists)"
    exit 0
fi

echo "Patching quarkus-roq-plugin-asciidoc-jruby ${ROQ_VERSION}..."

# Ensure JAR exists
if [ ! -f "${ASCIIDOC_RUNTIME_JAR}" ]; then
    echo "ERROR: JAR not found at ${ASCIIDOC_RUNTIME_JAR}"
    echo "Run 'mvn dependency:resolve' first"
    exit 1
fi

# Create temp directory
TEMP_DIR=$(mktemp -d)
trap "rm -rf ${TEMP_DIR}" EXIT

# Extract the JAR
echo "Extracting JAR..."
cd "${TEMP_DIR}"
jar xf "${ASCIIDOC_RUNTIME_JAR}"

# Find the .class file to patch
CLASS_FILE="io/quarkiverse/roq/plugin/asciidoctorj/runtime/AsciidoctorJConverter.class"

if [ ! -f "${CLASS_FILE}" ]; then
    echo "ERROR: Class file not found in JAR"
    exit 1
fi

# Extract and patch the Java source if available (most JARs don't include source)
# We'll use javap to decompile, patch the bytecode directly, or recompile from source
# For now, we'll use a simpler approach: download the source and recompile

# Get the roq source
echo "Downloading roq source..."
ROQSRC_DIR="${TEMP_DIR}/roq-src"
git clone --depth 1 --branch ${ROQ_VERSION} https://github.com/quarkiverse/quarkus-roq.git "${ROQSRC_DIR}" 2>&1 | grep -v "You are in 'detached HEAD' state" || true

if [ ! -d "${ROQSRC_DIR}/roq-plugin/asciidoc-jruby/runtime/src/main/java" ]; then
    echo "ERROR: Could not clone roq source"
    exit 1
fi

# Patch the source file
JAVA_FILE="${ROQSRC_DIR}/roq-plugin/asciidoc-jruby/runtime/src/main/java/io/quarkiverse/roq/plugin/asciidoctorj/runtime/AsciidoctorJConverter.java"

if [ ! -f "${JAVA_FILE}" ]; then
    echo "ERROR: AsciidoctorJConverter.java not found"
    exit 1
fi

echo "Patching AsciidoctorJConverter.java..."
# Change .safe(SafeMode.SAFE) to .safe(SafeMode.UNSAFE)
# Use a temporary file for cross-platform compatibility (macOS/BSD sed vs GNU sed)
sed 's/\.safe(SafeMode\.SAFE)/\.safe(SafeMode.UNSAFE)/g' "${JAVA_FILE}" > "${JAVA_FILE}.tmp"
mv "${JAVA_FILE}.tmp" "${JAVA_FILE}"

# Verify the patch was applied
if ! grep -q "\.safe(SafeMode\.UNSAFE)" "${JAVA_FILE}"; then
    echo "ERROR: Patch was not applied successfully"
    exit 1
fi

echo "Recompiling patched class..."

# Build the roq module to get proper classpath
echo "Building roq module to resolve classpath..."
cd "${ROQSRC_DIR}/roq-plugin/asciidoc-jruby/runtime"
mvn -q dependency:build-classpath -Dmdep.outputFile="${TEMP_DIR}/classpath.txt" -DincludeScope=compile

if [ ! -f "${TEMP_DIR}/classpath.txt" ]; then
    echo "ERROR: Failed to build classpath"
    exit 1
fi

CP=$(cat "${TEMP_DIR}/classpath.txt")

# Compile all related classes in the runtime package
RUNTIME_SRC="${ROQSRC_DIR}/roq-plugin/asciidoc-jruby/runtime/src/main/java/io/quarkiverse/roq/plugin/asciidoctorj/runtime"
javac -cp "${CP}" -d "${TEMP_DIR}/compiled" \
    "${RUNTIME_SRC}/AsciidoctorJConfig.java" \
    "${RUNTIME_SRC}/AsciidoctorJConverter.java" \
    "${RUNTIME_SRC}/AsciidocJInclude.java"

if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed"
    exit 1
fi

# Replace the class file in the JAR
echo "Replacing class file in JAR..."
cp "${TEMP_DIR}/compiled/${CLASS_FILE}" "${TEMP_DIR}/${CLASS_FILE}"

# Rebuild the JAR
cd "${TEMP_DIR}"
jar cf "${ASCIIDOC_RUNTIME_JAR}" .

# Create marker file
touch "${MARKER_FILE}"

echo "✓ Roq AsciidoctorJConverter patched successfully"
echo "  Changed SafeMode.SAFE to SafeMode.UNSAFE"
