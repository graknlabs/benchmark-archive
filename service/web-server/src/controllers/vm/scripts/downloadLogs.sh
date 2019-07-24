#!/bin/bash

GCLOUD_CREDENTIALS=$1
VM_NAME=$2
ZONE=$3


mkdir -p logs/$VM_NAME

gcloud auth activate-service-account --key-file $GCLOUD_CREDENTIALS

gcloud compute scp --zone=$ZONE ubuntu@$VM_NAME:~/execute.log ./logs/$VM_NAME/
gcloud compute scp --zone=$ZONE ubuntu@$VM_NAME:~/grakn/bazel-genfiles/grakn-core-all-linux/logs/grakn.log ./logs/$VM_NAME/
gcloud compute scp --zone=$ZONE ubuntu@$VM_NAME:~/grakn/bazel-genfiles/grakn-core-all-linux/logs/cassandra.log ./logs/$VM_NAME/