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

STOUT_LOG=~/logs/launch_executor_$INSTANCE_NAME_stdout.log
STERR_LOG=~/logs/launch_executor_$INSTANCE_NAME_stderr.log

ZONE="us-east1-b"

echo "Creating google cloud compute instance $INSTANCE_NAME..." | tee -a $STDOUT_LOG

#TODO: update metadata for project ssh keys
#for now it will keep trying to Updating project ssh metadata... and fail every time we try to ssh in it
gcloud compute instances create $INSTANCE_NAME          \
    --image-family grakn-benchmark-executor             \
    --image-project grakn-dev                           \
    --machine-type n1-standard-8                        \
    --zone=$ZONE                                        \
    > >(tee -a $STDOUT_LOG) 2> >(tee -a $STDERR_LOG >&2) 



echo "Waiting for $INSTANCE_NAME to be up and running..." | tee -a $STDOUT_LOG

RET=1
while [ $RET -ne 0 ]; do
    sleep 1;
    gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='true' > >(tee -a $STDOUT_LOG) 2> >(tee -a $STDERR_LOG >&2) 
    RET=$?; # collect return code
done

echo "Copying executor files to $INSTANCE_NAME..." | tee -a $STDOUT_LOG
gcloud compute scp --recurse ~/executor ubuntu@$INSTANCE_NAME:~ --zone=$ZONE > >(tee -a $STDOUT_LOG) 2> >(tee -a $STDERR_LOG >&2) 

# collect this instance's IP
echo "Retrieving this instance's IP..." | tee -a $STDOUT_LOG
THIS_IP=`curl -H "Metadata-Flavor: Google" http://169.254.169.254/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip > >(tee -a $STDOUT_LOG) 2> >(tee -a $STDERR_LOG >&2)`

# install tmux so we can detach and set script execution permissions
echo "Initialising new instance..." | tee -a $STDOUT_LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='chmod +x ~/executor/execute.sh && mkdir -p ~/logs' > >(tee -a $STDOUT_LOG) 2> >(tee -a $STDERR_LOG >&2)

# execute benchmark
echo "Starting benchmark..." | tee -a $STDOUT_LOG
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command=" \
    tmux new -d -s execute      \
        \" ~/executor/execute.sh $REPO_URL $COMMIT $THIS_IP $INSTANCE_NAME $EXECUTION_ID > >(tee -a ~/logs/executor_stdout.log) 2> >(tee -a ~/logs/executor_stderr.log >&2) \" \
    "
