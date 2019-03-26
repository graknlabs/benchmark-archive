#!/bin/bash

git clone https://github.com/graknlabs/grakn.git

cd grakn
bazel build //:assemble-linux-targz

cd bazel-genfiles
tar -xf grakn-core-all-linux.tar.gz

cd grakn-core-all-linux
./grakn server start