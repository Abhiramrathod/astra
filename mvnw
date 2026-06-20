#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper - Start Up Batch script
# ----------------------------------------------------------------------------

MAVEN_OPTS="${MAVEN_OPTS:-}"
MAVEN_SKIP_RC="${MAVEN_SKIP_RC:-false}"

if [ "$MAVEN_SKIP_RC" = "false" ]; then
  [ -f /usr/local/etc/mavenrc ] && . /usr/local/etc/mavenrc
  [ -f /etc/mavenrc ] && . /etc/mavenrc
  [ -f "$HOME/.mavenrc" ] && . "$HOME/.mavenrc"
fi

# Resolve JAVA_HOME
if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
  elif [ -x "$JAVA_HOME/bin/java.exe" ]; then
    JAVACMD="$JAVA_HOME/bin/java.exe"
  else
    echo "Error: JAVA_HOME set but no java executable in $JAVA_HOME/bin" >&2
    exit 1
  fi
else
  JAVACMD="java"
fi

SCRIPTPATH="$(cd "$(dirname "$0")" && pwd -P)"
WRAPPER_JAR="$SCRIPTPATH/.mvn/wrapper/maven-wrapper.jar"
MAVEN_PROJECTBASEDIR="$SCRIPTPATH"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Error: Maven wrapper JAR not found at $WRAPPER_JAR" >&2
  exit 1
fi

# On WSL, convert Linux paths to Windows paths for the Windows Java executable
if command -v wslpath >/dev/null 2>&1; then
  WRAPPER_JAR=$(wslpath -w "$WRAPPER_JAR")
  MAVEN_PROJECTBASEDIR=$(wslpath -w "$MAVEN_PROJECTBASEDIR")
fi

exec "$JAVACMD" $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
