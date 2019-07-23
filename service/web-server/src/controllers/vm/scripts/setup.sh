#!/bin/bash

GCLOUD_CREDENTIALS=$1
VM_NAME=$2
ZONE=$3
REPO_URL=$4
COMMIT=$5

gcloud auth activate-service-account --key-file $GCLOUD_CREDENTIALS
gcloud compute ssh ubuntu@$VM_NAME --zone=$ZONE --command="
    # navigate to home
    cd /home/ubuntu

    # build and unzip grakn
    git clone $REPO_URL
    cd grakn
    sudo git checkout $COMMIT
    sudo bazel build //:assemble-linux-targz
    cd bazel-genfiles
    sudo tar -xf grakn-core-all-linux.tar.gz

    # build and unzip benchmark
    cd /home/ubuntu
    git clone https://github.com/graknlabs/benchmark.git
    cd benchmark
    bazel build //:profiler-distribution
    cd bazel-genfiles
    unzip profiler.zip
"