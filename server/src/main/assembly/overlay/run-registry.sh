#!/bin/bash
#sets the classpath and starts the registry on port 1099
shopt -s nullglob

jars="."
for dep in lib/jars/*.jar; do
    jars="$jars:$dep"
done

rmiregistry -J-Djava.rmi.server.logCalls=true -J-Djava.class.path="$jars" "$@"