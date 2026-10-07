#!/bin/sh
# Compile and run the simulator from a terminal (Linux / macOS). Requires JDK 11+ (17+ recommended).
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -d bin $(find src -name "*.java")
java -cp bin main.Main
