#!/bin/bash

GCLOUD_CREDENTIALS=$1
VM_NAME=$2
ZONE=$3

gcloud auth activate-service-account --key-file $GCLOUD_CREDENTIALS
gcloud compute ssh ubuntu@$VM_NAME --zone=$ZONE --command='cd /home/ubuntu/benchmark/bazel-genfiles/profiler && tmux new-session -d -s zipkin "STORAGE_TYPE=elasticsearch ES_HOSTS=$ES_URI ES_INDEX=benchmark java -jar external-dependencies/zipkin.jar"'