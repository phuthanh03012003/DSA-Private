#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
mkdir -p out
if command -v javac >/dev/null 2>&1; then
  javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d out hashing_strings/*.java
else
  java -m jdk.compiler/com.sun.tools.javac.Main --release 17 -encoding UTF-8 -Xlint:all -Werror -d out hashing_strings/*.java
fi
java -cp out hashing_strings.AllTests "$@"
