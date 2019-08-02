#!/bin/bash

GCLOUD_CREDENTIALS=$1
VM_NAME=$2
ZONE=$3
EXECUTOR_FILE=$4
COMMIT=$5
ES_URI=$6
WEB_URI=$7
REPO_URL=$8
EXECUTION_ID=$9

gcloud auth activate-service-account --key-file $GCLOUD_CREDENTIALS

gcloud compute scp --recurse $EXECUTOR_FILE ubuntu@$VM_NAME:~ --zone=$ZONE

gcloud compute ssh ubuntu@$VM_NAME --zone=$ZONE --command=" \
    chmod +x ~/execute.sh
    tmux new -d -s execute      \
        \" ~/execute.sh $REPO_URL $COMMIT $ES_URI $WEB_URI $VM_NAME $EXECUTION_ID 2>&1 | tee -a ~/execute.log \" \
    "