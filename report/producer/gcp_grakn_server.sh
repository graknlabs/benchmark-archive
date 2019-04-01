#!/bin/bash

echo "Cloning Grakn"
git clone https://github.com/graknlabs/grakn.git

cd grakn
echo "Building Grakn"
bazel build //:assemble-linux-targz
echo "Building Grakn again... (attempt at fixing nondeterministic bug about missing `jre/lib/amd64/server/classes.jsa`"
bazel build //:assemble-linux-targz

cd bazel-genfiles
echo "Untarring Grakn"
tar -xf grakn-core-all-linux.tar.gz

cd grakn-core-all-linux
echo "Starting Grakn with nohup"
nohup ./grakn server start &

# for some reason, if we don't wait here forever grakn never starts
cat