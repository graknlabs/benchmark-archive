#!/bin/bash

GCLOUD_CREDENTIALS=$1
VM_NAME=$2
ZONE=$3
LOG_DIR=$4


mkdir -p logs/$VM_NAME

gcloud auth activate-service-account --key-file $GCLOUD_CREDENTIALS

gcloud compute scp --zone=$ZONE ubuntu@$VM_NAME:~/execute.log $LOG_DIR/$VM_NAME/
gcloud compute scp --zone=$ZONE ubuntu@$VM_NAME:~/grakn/bazel-genfiles/grakn-core-all-linux/logs/grakn.log $LOG_DIR/$VM_NAME/
gcloud compute scp --zone=$ZONE ubuntu@$VM_NAME:~/grakn/bazel-genfiles/grakn-core-all-linux/logs/cassandra.log $LOG_DIR/$VM_NAME/