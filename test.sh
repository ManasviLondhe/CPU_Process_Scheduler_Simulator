#!/bin/sh
# Compile everything and run the test-suite.
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -d bin $(find src -name "*.java")
java -cp bin tests.TestRunner
