#!/bin/sh
# Runs target/vecdsl-cli.jar with the java of JAVA_HOME (else the PATH). From JDK 24 on, passes
# --sun-misc-unsafe-memory-access=allow (JEP 498), so Guice 7.0.0 prints no Unsafe warning; JDK 21 rejects the option.
dir=$(cd "$(dirname "$0")" && pwd)
java=${JAVA_HOME:+$JAVA_HOME/bin/}java
major=$("$java" -version 2>&1 | sed -n 's/.* version "\([0-9]*\)[."].*/\1/p' | head -n 1)
jar=$dir/../target/vecdsl-cli.jar
if [ "${major:-0}" -ge 24 ]; then
	exec "$java" --sun-misc-unsafe-memory-access=allow -jar "$jar" "$@"
fi
exec "$java" -jar "$jar" "$@"
