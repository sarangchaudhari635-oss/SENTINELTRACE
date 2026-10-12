#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Script for Unix/Linux/macOS
# ----------------------------------------------------------------------------
set -e
MAVEN_VERSION="3.9.9"
MAVEN_DIR="$HOME/.m2/wrapper/dists/apache-maven-$MAVEN_VERSION"
MVN_CMD="$MAVEN_DIR/apache-maven-$MAVEN_VERSION/bin/mvn"

if [ ! -x "$MVN_CMD" ]; then
    echo "Downloading Apache Maven $MAVEN_VERSION..."
    mkdir -p "$MAVEN_DIR"
    curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip" -o "$MAVEN_DIR/maven.zip"
    unzip -q -o "$MAVEN_DIR/maven.zip" -d "$MAVEN_DIR"
    chmod +x "$MVN_CMD"
fi

exec "$MVN_CMD" "$@"
