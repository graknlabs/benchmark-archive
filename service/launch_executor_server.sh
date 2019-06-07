#!/bin/bash

if [ $# -ne 4 ]
then
    echo "No arguments supplied"
    echo "Usage: ./launch_executor_server.sh <repo https url> <execution id> <commit id> <new vm name>"
    exit 1;
fi


REPO_URL=$1
EXECUTION_ID=$2
COMMIT=$3
INSTANCE_NAME=$4

LOG=~/logs/launch_executor_$INSTANCE_NAME.log

ZONE="us-east1-b"

echo "Creating google cloud compute instance $INSTANCE_NAME..." | tee -a $LOG

#TODO: update metadata for project ssh keys
#for now it will keep trying to Updating project ssh metadata... and fail every time we try to ssh in it
gcloud compute instances create $INSTANCE_NAME          \
    --image benchmark-executor-image-2             \
    --image-project grakn-dev                           \
    --machine-type n1-standard-16                        \
    --zone=$ZONE                                        \
    2>&1 | tee -a $LOG


echo "Waiting for $INSTANCE_NAME to be up and running..." | tee -a $LOG

RET=1
while [ $RET -ne 0 ]; do
    sleep 1;
    gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='true' 2>&1 | tee -a $LOG
    RET=$?; # collect return code
done

echo "Copying executor files to $INSTANCE_NAME..." | tee -a $LOG
gcloud compute scp --recurse ~/executor ubuntu@$INSTANCE_NAME:~ --zone=$ZONE 2>&1 | tee -a $LOG

# collect this instance's IP
echo "Retrieving this instance's IP..." | tee -a $LOG
THIS_IP=`curl -H "Metadata-Flavor: Google" http://169.254.169.254/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip`

# install tmux so we can detach and set script execution permissions
echo "Initialising new instance..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='chmod +x ~/executor/execute.sh && mkdir -p ~/logs' 2>&1 | tee -a $LOG

# execute benchmark
echo "Starting benchmark..." | tee -a $LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command=" \
    tmux new -d -s execute      \
        \" ~/executor/execute.sh $REPO_URL $COMMIT $THIS_IP $INSTANCE_NAME $EXECUTION_ID 2>&1 | tee -a ~/logs/executor.log \" \
    "
