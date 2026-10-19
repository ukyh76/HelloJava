#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$PROJECT_DIR/target/web-classes"

if command -v javac >/dev/null 2>&1; then
  JAVAC_BIN="$(command -v javac)"
  JAVA_BIN="$(command -v java)"
elif [ -n "${CAMPUS_JDK_HOME:-}" ] && [ -x "$CAMPUS_JDK_HOME/bin/javac" ]; then
  JAVAC_BIN="$CAMPUS_JDK_HOME/bin/javac"
  JAVA_BIN="$CAMPUS_JDK_HOME/bin/java"
else
  echo "未找到 JDK 21。请安装 JDK，并确保 javac 在 PATH 中。" >&2
  echo "也可以通过 CAMPUS_JDK_HOME 指定 JDK 安装目录。" >&2
  exit 1
fi

mkdir -p "$BUILD_DIR"
find "$PROJECT_DIR/src/main/java" -name '*.java' -print0 \
  | xargs -0 "$JAVAC_BIN" --release 21 --add-modules jdk.httpserver -encoding UTF-8 -d "$BUILD_DIR"

if [ -d "$PROJECT_DIR/src/main/resources" ]; then
  cp -R "$PROJECT_DIR/src/main/resources/." "$BUILD_DIR/"
fi

exec "$JAVA_BIN" --add-modules jdk.httpserver -cp "$BUILD_DIR" com.campus.web.WebApp
